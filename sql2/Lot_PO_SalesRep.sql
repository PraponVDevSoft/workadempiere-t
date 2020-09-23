--select * from M_storage

select 
  --m.m_attributesetinstance_id,
  --asi.lot,
  --o.documentno,
  o.SalesRep_ID
from m_storage m
left join m_attributesetinstance asi on m.m_attributesetinstance_id = asi.m_attributesetinstance_id
left join c_order o on o.documentno = upper(asi.lot)

--where COALESCE(asi.lot,'') <>''