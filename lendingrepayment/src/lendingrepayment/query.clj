(ns lendingrepayment.query
  (:require  [taoensso.timbre :as timbre]
             [clojure.data.json :as json]
             [korma.db :refer :all]
             [lendingrepayment.config :as config]
             [clj-time.coerce :as tc]
             [clojure.string :as string]
             [clojure.walk :as walk]
             [korma.core :refer :all]
             [clj-time.core :as t]
             [clj-time.format :as f]
             [clj-time.coerce :as tc]
             [clj-time.local :as l]
             [clojure.java.io :as io]
             [clojure.math.numeric-tower :as math]
             [ring.util.http-response :refer :all]

             )
  )

(config/initialize-config)

(def db-connection-lendingrepay
  {:classname "org.postgresql.Driver"
   :subprotocol config/db-subprotocol
   :user config/db-user
   :password config/db-pass
   :subname config/db-subname
   })


(defn elevator1 []
  "el1 = Print Loop from 100 to 1"
  (loop [counter 10]
    (when (pos? counter)
      (do
         (Thread/sleep 1500)
        (println (str "el1 counter: " counter))
        (recur (dec counter))))))


(defn elevator2 [n]
  "el2 = Print Loop from n to 1"
  (loop [counter n]
    (when (pos? counter)
      (do
        (Thread/sleep 1000)
        (println (str "el2 counter: " counter))
        (recur (dec counter))))))



(defn request-to-keywords [req]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" req)]
             [(keyword k) v])))

(def alphanumeric "ABCDEFGHJKLMNPQRSTUVWXYZ1234567890")
(defn get-random-id [length]
  (loop [acc []]
    (if (= (count acc) length) (apply str acc)
                               (recur (conj acc (rand-nth alphanumeric))))))

(defn get-current-timestamp []
  (str (f/unparse (f/formatter-local "yyyy-MM-dd HH:mm:ss") (l/local-now)))
  )

(defn get-subscribers [body]
  (let [subsc (with-db db-connection-lendingrepay (exec-raw (format "SELECT details::text subscribers
                                                FROM tbl_subscribers
                                                WHERE details->>'msisdn'::text = '%s';" (:msisdn body)) :results)
                           )]
    subsc
    )
  )

(defn create-subscriber [body]
  (let [
        refno (str (get-random-id 8))
        ]
    (with-db db-connection-lendingrepay (exec-raw (format "INSERT INTO tbl_subscribers (refno, details)
                                                              VALUES ('%s','%s');"
                                                    refno
                                                    (string/replace (json/write-str body) #"'" "''")
                                                    )))

    (get-subscribers refno)
    )
  )

(defn get-loan-types [body]
  (let [loantypes (with-db db-connection-lendingrepay (exec-raw (format "SELECT details::text loantypes
                                                FROM tbl_loantypes;"
                                                                        ) :results)
                           )]
    loantypes
    )
  )

(defn create-loan-types [body]
  (let [
        refno (str (get-random-id 8))
        ]
    (with-db db-connection-lendingrepay (exec-raw (format "INSERT INTO tbl_loantypes (refno, details)
                                                              VALUES ('%s','%s');"
                                                          refno
                                                          (string/replace (json/write-str body) #"'" "''")
                                                          )))

    (get-loan-types refno)
    )
  )


(defn get-loans [body]
  (if (or (contains? body :refno) (contains? body :loanref))
    (with-db db-connection-lendingrepay (exec-raw (format "SELECT details::text loandetails --, repayments::text repayments, sum(coalesce((rpmnts->>'amount')::float, 0)) AS repayments
                                                            FROM tbl_loans --, jsonb_array_elements(repayments) with ordinality arr(rpmnts, index)
                                                            WHERE details->>'refno'::text = '%s'
                                                            GROUP BY 1 --,2
                                                            , id
                                                            ORDER BY id DESC;" (or (:refno body) (:loanref body))) :results)
             )

    (if (contains? body :msisdn)
      (with-db db-connection-lendingrepay (exec-raw (format "SELECT details::text loandetails --, repayments::text repayments
      --, sum(coalesce((rpmnts->>'amount')::float, 0)) AS repayments
                                                                FROM tbl_loans --, jsonb_array_elements(repayments) with ordinality arr(rpmnts, index)
                                                                WHERE details->>'msisdn'::text = '%s'
                                                                GROUP BY 1 --,2
                                                                , id
                                                                ORDER BY id DESC;" (:msisdn body)) :results)
               )

      )
    )
  )

(defn create-loan [body]
  (let [;check qualification
        sub (first (get-subscribers {:msisdn (:msisdn body)}))
        sub (walk/keywordize-keys (json/read-str (:subscribers sub)))
        repayments (:repayments (first (with-db db-connection-lendingrepay (exec-raw (format "SELECT CASE WHEN EXISTS( SELECT sum(coalesce((rpmnts->>'amount')::float, 0))
                                                                                          FROM tbl_loans, jsonb_array_elements(repayments) with ordinality arr(rpmnts, index)
                                                                                            WHERE details->>'msisdn'::text = '%s') then 1 else 0 end as repayments;" (:msisdn sub)) :results)
                                                )
                                       )
                     )
        loans (:loans (first (with-db db-connection-lendingrepay (exec-raw (format "SELECT sum(coalesce((details->>'amount'::text)::real, 0)) loans
                                                                                          FROM tbl_loans
                                                                                            WHERE details->>'msisdn'::text = '%s';" (:msisdn sub)) :results)
                                                )
                                       )
                     )
        amtborrowable (- (:qualification sub) (- loans repayments))


        ]

    (if (and (> amtborrowable 0) (>= amtborrowable (:amount body)))
      (let [refno (str (get-random-id 8))
            body (assoc body :loantyperef "abc123")
            body (assoc body :refno refno)
            body (assoc body :date (get-current-timestamp))
            body (assoc body :date_due (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")
                                                (tc/to-timestamp (t/plus (f/parse (f/formatters :mysql)
                                                                                  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")
                                                                                           (new java.util.Date)))
                                                                         (t/days 7)))))
            body (assoc body :active true)
            body (assoc body :overdue false)
            body (assoc body :bad_loan false)]
        (with-db db-connection-lendingrepay (exec-raw (format "INSERT INTO tbl_loans (refno, details)
                                                              VALUES ('%s','%s');"
                                                              refno
                                                              (string/replace (json/write-str body) #"'" "''")
                                                              )))

        (get-loans {:loanref refno})
        )
      )
    "unpaid"
    )
  )

(defn repay [body]
  (let [
        refno (str (get-random-id 8))
        body (assoc body :repayid refno)
        repayments (:repayments (first (with-db db-connection-lendingrepay (exec-raw (format "SELECT CASE WHEN EXISTS( SELECT sum(coalesce((rpmnts->>'amount')::float, 0))
                                                                                          FROM tbl_loans, jsonb_array_elements(repayments) with ordinality arr(rpmnts, index)
                                                                                            WHERE details->>'msisdn'::text = '%s'
                                                                                            AND details->>'refno'::text = '%s') then 1 else 0 end as repayments;" (:msisdn body) (:refno body)) :results)
                                            )
                                   )
                     )

        amtdue (- (:amtdue (first (with-db db-connection-lendingrepay (exec-raw (format "SELECT COALESCE(SUM(((details->>'amount'::text)::real)),0)::real amtdue
                                                      FROM tbl_loans
                                                      WHERE details->>'refno'::text = '%s';" repayments (:refno body)) :results)
                                           )
                                  )
                    ) repayments)
        _ (if (> (:amount body) amtdue) (assoc body :overpayment (- (:amount body) amtdue)) (assoc body :overpayment 0))
        ]
    (with-db db-connection-lendingrepay (exec-raw (format "UPDATE tbl_loans
                                                      SET repayments = COALESCE(repayments, '[]'::jsonb) || '%s' ::jsonb
                                                      WHERE refno = '%s';"
                                                    (string/replace (json/write-str [body]) #"'" "''")
                                                    (:loanref body)
                                                    ) ))

    (get-loans {:loanref (:loanref body)})
    )
  )





