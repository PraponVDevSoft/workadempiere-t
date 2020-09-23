package org.tkgarment.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_M_InventoryLine;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MStorage;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.model.X_C_UOM;
import org.compiere.model.X_C_UOM_Conversion;
import org.zkoss.zhtml.Big;

public class TKCallout extends CalloutEngine {
	public String internalInventoryUseChanged (	Properties ctx, int WindowNo, 
			GridTab mTab, 
			GridField mField, 
			Object value) {
		
		int M_Locator_ID = (Integer)mTab.getField(I_M_InventoryLine.COLUMNNAME_M_Locator_ID).getValue();
		int M_Product_ID = (Integer)mTab.getField(I_M_InventoryLine.COLUMNNAME_M_Product_ID).getValue();
		int M_AttributeSetInstance_ID = (Integer)mTab.getField(I_M_InventoryLine.COLUMNNAME_M_AttributeSetInstance_ID).getValue();
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer whereClause = new StringBuffer();
		
		String tableName = "M_Storage";
		whereClause.append(" M_Locator_ID = ? ");
		  params.add(M_Locator_ID);
		whereClause.append(" and M_Product_ID = ? ");
		  params.add(M_Product_ID);
		whereClause.append(" and M_AttributeSetInstance_ID = ? ");
		  params.add(M_AttributeSetInstance_ID);
		
		Query query = new Query(ctx, tableName, whereClause.toString(), null).setParameters(params);
		MStorage mStorage = query.first();
		double qtyAvailable = 0;
		if(mStorage != null){
			qtyAvailable = mStorage.getQtyOnHand().doubleValue() -  mStorage.getQtyReserved().doubleValue();
		}
		
		double reqValue = Double.valueOf(value.toString());
		
		if(reqValue > qtyAvailable ){
			
			return "ยอดคงเหลือที่สามารถเบิกได้   " + qtyAvailable;
		}
		return "";
		
	}
	
	public String purchaseUnitChanged (	Properties ctx, int WindowNo, 
										GridTab mTab, 
										GridField mField, 
										Object value) {
		String msg = "";
		int fromUomId = -1; //1000022
		int toUomId = -1; //1000002
		double qtyPurchase = 0.0;
//		GridField qtyField = mTab.getField("QtyPurchase");
		try{
			double qtyRequire = ((BigDecimal) mTab.getField("QtyRequired").getValue()).doubleValue();
			fromUomId = (Integer)mTab.getField("C_UOM_ID").getValue();
			System.out.println("fromUomId : " + fromUomId);
			toUomId = (Integer)value;
			System.out.println("toUomId : " + toUomId);
			if(fromUomId == toUomId){
				qtyPurchase = qtyRequire;
//				qtyField.setValue(qtyPurchase,true);
				mTab.setValue("QtyPurchase", qtyPurchase);
				
				return "";
			}
			
			MTable table = MTable.get(ctx, "C_UOM_Conversion");
			String whereClause = "C_UOM_ID = "+fromUomId+" and C_UOM_To_ID = "+toUomId;
			Query query = new Query(ctx, table, whereClause, null);
			X_C_UOM_Conversion uomConvert = query.first();
			if(uomConvert != null){
				double divideRate = uomConvert.getDivideRate().doubleValue();
				System.out.println("divide rate : " + divideRate);
				qtyPurchase = Math.ceil(qtyRequire / divideRate);
				System.out.println("Qty purchase : " + qtyPurchase );
			}
//			qtyField.setValue(qtyPurchase, true);
			mTab.setValue("QtyPurchase", qtyPurchase);

			
		}catch(Exception e){
//			qtyField.setValue(0, true);
			mTab.setValue("QtyPurchase", 0);
			e.printStackTrace();
				
		}
		
		
		
		return msg;
	}
	public static void main(String[] args) {
		
	}
	public String docStatusChanged (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
//		System.out.println("WindowNo " + WindowNo);
//		System.out.println("mTab " + mTab);
//		System.out.println("mField " + mField);
//		System.out.println("value " + value);
		
		if(value.equals("DR")){
			mTab.getField("Description").setValue("Draft", true);
			
		}else{
			mTab.getField("Description").setValue("hello", true);
		}
		
		return "";
	}
}
