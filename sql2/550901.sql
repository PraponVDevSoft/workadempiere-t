select * from pp_product_bom where name like 'test-01' -- 1001200
select * from 
 where pp_product_bom_id = 1001200
select * from pp_product_bomline where pp_product_bom_id = 1001200

select * from m_inventory where m_inventory_id = 1000211


SELECT AD_Table_ID FROM AD_Table 
WHERE (TableName IN ('RV_WarehousePrice','RV_BPartner') OR IsView='N') 
	AND IsActive = 'Y' 
	AND TableName NOT LIKE '%_Trl' 
	AND EntityType IN ('D') 
	AND TableName LIKE 'M_Product' ORDER BY TableName

select * from ad_table where ad_table_id = 208

SELECT AD_Table_ID 
FROM AD_Table WHERE (TableName IN ('RV_WarehousePrice','RV_BPartner') OR IsView='N') 
	AND IsActive = 'Y'  
	AND TableName NOT LIKE '%_Trl' 
	AND EntityType IN ('D') AND TableName LIKE 'M_Product' ORDER BY TableName

select * from m_product

select * from ad_table where name like '%Manu%'

select * from m_product