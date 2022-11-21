(ns lendingrepayment.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [lendingrepayment.query :as query]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json]
            [clojure.walk :as walk]))

(defroutes app-routes
           (GET "/" [] "Hello World")

           (GET "/subscribers" request
             ;/subscribers?msisdn=254722111111
             (let [paramsmap (query/request-to-keywords (get-in request [:query-string]))
                   subsc (query/get-subscribers paramsmap)
                   ]
               (json/write-str {:data {
                                       :status 200
                                       :title (str "Subscribers")
                                       :details subsc
                                       }
                                })
               )
             )

           (POST "/subscribers/create" request
             (let [body (walk/keywordize-keys (:body request))
                   subsc (query/create-subscriber body)
                   ]
               (json/write-str {:data {
                                       :status 201
                                       :title (str "Create Subscriber")
                                       :details subsc
                                       }
                                })
               )
             )

           (GET "/loans" request
             ;/loans?msisdn=254722111111
             (timbre/info "getting loan details ")
             (let [paramsmap (query/request-to-keywords (get-in request [:query-string]))
                   subsc (query/get-loans paramsmap)
                   ]
               (json/write-str {:data {
                                       :status 200
                                       :title (str "Loans")
                                       :details subsc
                                       }
                                })
               )
             )

           (GET "/loan-types" request
             ;/loan-types?market=kenya&active=true
             (let [paramsmap (query/request-to-keywords (get-in request [:query-string]))
                   subsc (query/get-subscribers paramsmap)
                   ]
               (json/write-str {:data {
                                       :status 200
                                       :title (str "Loan Types")
                                       :details subsc
                                       }
                                })
               )
             )

           (POST "/borrow" request
             (let [body (walk/keywordize-keys (:body request))
                   brrw (query/create-loan body)
                   ]
               (if (= brrw "unpaid")
                 (json/write-str {:errors {
                                         :status 424
                                         :title (str "Borrow Failed")
                                         :details "Previous loan(s) not repaid"
                                         }
                                  })
                 (json/write-str {:data {
                                         :status 200
                                         :title (str "Borrow")
                                         :details brrw
                                         }
                                  })
                 )

               )
             )

           (POST "/repay" request
             (let [body (walk/keywordize-keys (:body request))
                   rpy (query/repay body)
                   ]
               (json/write-str {:data {
                                       :status 200
                                       :title (str "Repay")
                                       :details rpy
                                       }
                                })
               )
             )


           (GET "/elevators" request
             (let [

                   ]
               (newline)
               (.start (Thread. query/elevator1))
               (Thread/sleep (* 2 1000))

               (newline)
               (.start (Thread. #(query/elevator2 5)))
               (Thread/sleep (* 2 1000))
               (newline)
               (println :done)
               )
             )

           (route/not-found "Not Found"))

(def app
  (-> app-routes
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
      (wrap-defaults app-routes)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:post :get]
                 :access-control-allow-header ["Access-Control-Allow-Origin" "*"
                                               "Origin" "X-Requested-With"
                                               "Content-Type" "Accept"]
                 )

      )
  )
