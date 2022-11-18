CREATE TABLE IF NOT EXISTS "tbl_loantypes"
(
  id bigserial,
  refno text,
  details jsonb DEFAULT '[]'::jsonb,
  created timestamp with time zone default now(),
  CONSTRAINT tbl_loantypes_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "tbl_loantypes"
  OWNER TO postgres;

  CREATE TABLE IF NOT EXISTS "tbl_loans"
  (
    id bigserial,
    refno text,
    details jsonb DEFAULT '{}'::jsonb,
    created timestamp with time zone default now(),
    CONSTRAINT tbl_loans_pkey PRIMARY KEY (id)
  )
  WITH (
    OIDS=FALSE
  );
  ALTER TABLE "tbl_loans"
    OWNER TO postgres;


  CREATE TABLE IF NOT EXISTS "tbl_subscribers"
  (
    id bigserial,
    refno text,
    details jsonb DEFAULT '{}'::jsonb,
    repayments jsonb DEFAULT '[]'::jsonb,
    created timestamp with time zone default now(),
    CONSTRAINT tbl_subscribers_pkey PRIMARY KEY (id)
  )
  WITH (
    OIDS=FALSE
  );
  ALTER TABLE "tbl_subscribers"
    OWNER TO postgres;

  CREATE TABLE IF NOT EXISTS "tbl_repayments"
  (
    id bigserial,
    refno text,
    details jsonb DEFAULT '{}'::jsonb,
    created timestamp with time zone default now(),
    CONSTRAINT tbl_repayments_pkey PRIMARY KEY (id)
  )
  WITH (
    OIDS=FALSE
  );
  ALTER TABLE "tbl_repayments"
    OWNER TO postgres;

    alter table tbl_loans add column repayments jsonb DEFAULT '[]'::jsonb