-- Function: transaction_periodsum_pt(numeric, timestamp without time zone, timestamp without time zone, numeric, numeric)

-- DROP FUNCTION transaction_periodsum_pt(numeric, timestamp without time zone, timestamp without time zone, numeric, numeric);

CREATE OR REPLACE FUNCTION transaction_periodsum_pt(
    p_ad_pinstance_id numeric,
    p_movementdatefrom timestamp without time zone,
    p_movementdateto timestamp without time zone,
    p_m_product_category_id numeric,
    p_m_product_id numeric)
  RETURNS integer AS
$BODY$
DECLARE
--	v_reccount	int = 0;
BEGIN
   --	v_ad_pinstance_id:=9999999;
  DELETE from t_transaction_periodsum_pt where (DATE_PART('epoch',getdate()-updated)/3600) > 12;
  /* delete where that record more than 12 hours.,  ad_pinstance_ID <> p_ad_pinstance_ID ; */

  INSERT INTO t_transaction_periodsum_pt 
     (ad_pinstance_id,ad_client_id,ad_org_id,created,createdby,updated,updatedby,isactive,m_product_id,m_product_category_id)
  SELECT p_ad_pinstance_id,ad_client_id,ad_org_id,created,createdby,updated,updatedby,isactive,m_product_id,m_product_category_id 
  FROM m_product;
  
  UPDATE t_transaction_periodsum_pt 
     SET movementdate = p_movementdatefrom,
         qtybringforward = 0,
         qtyin = 0,
         qtyout = 0,
         costaverage = 0
   WHERE t_transaction_periodsum_pt.ad_pinstance_id = p_ad_pinstance_id;

  UPDATE t_transaction_periodsum_pt
     SET m_product_category_id = m_product.m_product_category_id,
         isactive = m_product.isactive
    FROM m_product
   WHERE t_transaction_periodsum_pt.m_product_id = m_product.m_product_id and 
         t_transaction_periodsum_pt.updated <> m_product.updated;
         
/* movementdate,qtybringforward,qtyin,qtyout,costaverage    getdate()*/  

/*  ยอดยกมา */
  UPDATE t_transaction_periodsum_pt 
  SET qtybringforward = t.movementqty
  FROM (
    SELECT  m_product_id, sum(movementqty) as movementqty 
    FROM m_transaction
    WHERE movementdate<p_movementdatefrom
    GROUP BY m_product_id
  ) t
  WHERE t_transaction_periodsum_pt.m_product_id = t.m_product_id 
    AND t_transaction_periodsum_pt.ad_pinstance_id = p_ad_pinstance_id;

/* in */
  UPDATE t_transaction_periodsum_pt 
  SET qtyin = i.movementqty
  FROM (
  /*
    SELECT  m_product_id, sum(movementqty) as movementqty
    FROM m_transaction
    WHERE movementdate>=p_movementdatefrom and movementdate<=p_movementdateto
      AND movementqty>0
    GROUP BY m_product_id
   */
    select M_Product_ID, sum(movementqty) as movementqty from rv_inoutdetails
    WHERE movementdate>=p_movementdatefrom and movementdate<=p_movementdateto
      and docstatus = ANY (ARRAY['CO'::bpchar, 'CL'::bpchar]) 
    group by M_Product_ID    
  ) i
  WHERE t_transaction_periodsum_pt.m_product_id = i.m_product_id
    AND t_transaction_periodsum_pt.ad_pinstance_id = p_ad_pinstance_id;


    
/* out */
  UPDATE t_transaction_periodsum_pt 
  SET qtyout = o.movementqty
  FROM (
    SELECT  m_product_id, sum(movementqty) as movementqty
    FROM m_transaction
    WHERE movementdate>=p_movementdatefrom and movementdate<=p_movementdateto 
      AND movementqty<0
    GROUP BY m_product_id

    
  ) o
  WHERE t_transaction_periodsum_pt.m_product_id = o.m_product_id
    AND t_transaction_periodsum_pt.ad_pinstance_id = p_ad_pinstance_id;
qtybringforward = 0,
         qtyin = 0,    
   /* Cost Average 
  UPDATE t_transaction_sum a
   set costaverage = b.costaverage
  from (
    SELECT L.m_product_id, L.ponum, avg(COALESCE(L.priceactual, 0::numeric)) AS costaverage
    FROM t_ProductLotPrice_v_pt L
    GROUP BY L.m_product_id, L.ponum
  ) b
  where a.ad_pinstance_ID = p_ad_pinstance_ID and a.m_product_id = b.m_product_id and a.ponum=b.ponum;
  */
  
RETURN 1;

END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION transaction_periodsum_pt(numeric, timestamp without time zone, timestamp without time zone, numeric, numeric)
  OWNER TO adempiere;
