create view pp_product_bomline_onhand_pt_v as
SELECT 
  pl.m_product_id, 
  pl.pp_product_bom_id, 
  pl.pp_product_bomline_id, 
  pl.m_attributesetinstance_id, 
  pl.c_uom_id, 
  ms.m_locator_id, 
  ms.qtyonhand, 
  ms.qtyreserved, 
  ms.qtyordered
FROM 
  adempiere.pp_product_bomline pl, 
  adempiere.m_storage ms
WHERE 
  pl.m_product_id = ms.m_product_id AND
  pl.m_attributesetinstance_id = ms.m_attributesetinstance_id;
