/*
select * from m_transaction 
order by created desc
limit 10000

select movementtype from m_transaction group by movementtype
*/

select * from rv_inoutdetails
where  docstatus = ANY (ARRAY['CO'::bpchar, 'CL'::bpchar])
 and M_Product_ID=1000342
order by created desc
limit 1000



select M_Product_ID, sum(movementqty) as qty from rv_inoutdetails
--where -- M_Product_ID=1000342 and
   --docstatus = ANY (ARRAY['CO'::bpchar, 'CL'::bpchar]) 
group by M_Product_ID
order by M_Product_ID
  



order by created desc
limit 1000


 (o.docstatus = ANY (ARRAY['DR'::bpchar, 'CO'::bpchar, 'CL'::bpchar]))


 select * from m_inventoryline
where  --docstatus = ANY (ARRAY['CO'::bpchar, 'CL'::bpchar])  and 
M_Product_ID=1000342
order by created desc
limit 1000
