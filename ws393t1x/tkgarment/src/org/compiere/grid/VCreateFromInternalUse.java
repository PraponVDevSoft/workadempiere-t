package org.compiere.grid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.I_M_InventoryLine;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MStorage;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.eevolution.model.MPPProductBOM;
import org.eevolution.model.MPPProductBOMLine;

/**
 * Create Order from Material requirement
 * 
 * @author noum
 * 
 */
public class VCreateFromInternalUse extends CreateFrom {
	private String tag = "VCreateFromInternalUse";
	public VCreateFromInternalUse(GridTab gridTab) {
		super(gridTab);
		log.info(gridTab.toString());
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dynInit() throws Exception {
		// TODO Auto-generated method stub
		setTitle(Msg.getElement(Env.getCtx(), "M_Inventory_ID", false) + " .. "
				+ Msg.translate(Env.getCtx(), "CreateFrom"));
		return true;
	}

	@Override
	public void info() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean save(IMiniTable miniTable, String trxName) {
		// TODO Auto-generated method stub
		
		Properties ctx = Env.getCtx();
		int M_Inventory_ID = ((Integer) getGridTab().getValue("M_Inventory_ID")).intValue();
		log.info(tag + "(save) M_Inventory_ID : " + M_Inventory_ID);
		// Lines .. create internal use line and save it
		List<MInventoryLine> dupLine = new ArrayList<MInventoryLine>();
		for (int i = 0; i < miniTable.getRowCount(); i++) {
			if (((Boolean) miniTable.getValueAt(i, 0)).booleanValue()) {
				KeyNamePair pp = (KeyNamePair) miniTable.getValueAt(i, 1);
				String desc = (String)miniTable.getValueAt(i, 2);
				
				KeyNamePair attSetInsPair = (KeyNamePair)miniTable.getValueAt(i,3);
				int M_AttributeSetInstance_ID = attSetInsPair.getKey();
				
				
				int M_Product_ID = pp.getKey();
				
				MProduct product = new MProduct(ctx, M_Product_ID, null);
				
				
				MInventory inventory = new MInventory(ctx, M_Inventory_ID, null);
				MInventoryLine line = new MInventoryLine(	inventory, 
															product.getM_Locator_ID(), 
															M_Product_ID, 
															M_AttributeSetInstance_ID,//M_AttributeSetInstance_ID
															
															null, //QtyBook, 
															null); // QtyCount
				
				line.setDescription(desc);
				BigDecimal dbQtyRequired = (BigDecimal) miniTable.getValueAt(i, 5); 
				BigDecimal dbQtyAvailable = (BigDecimal) miniTable.getValueAt(i, 6);
				BigDecimal dbQtyUse;
				if ( dbQtyRequired.doubleValue() <= dbQtyAvailable.doubleValue()){
					dbQtyUse = dbQtyRequired;
				}else{
					dbQtyUse = dbQtyAvailable;
				}
				
				line.setQtyInternalUse(dbQtyUse);
				
				line.setC_Charge_ID(1000000); // "เบิกสินค้า/วัตถุดิบใช้ภายใน"
				
				if(duplicateInventoryLine(inventory, line)){
					dupLine.add(line);
				}else{
					line.saveEx();
				}
				/*
				MOrder order = new MOrder(ctx, M_Inventory_ID, null);
				MOrderLine orderLine = new MOrderLine(order);
				// orderLine.setC_UOM_ID(100); // uom_id: 100 - Each
				orderLine.setM_Product_ID(M_Product_ID, 100);
				orderLine.saveEx();
				*/
			}
		}
		StringBuilder msg = new StringBuilder();
		if(dupLine.size() != 0){
			msg.append("เลือกรายการซ้ำ\n");
			for(MInventoryLine line :dupLine){
				
				msg.append("วัตถุดิบ ["+ line.getM_Product_ID()+ " " + line.getProduct().getName() + "] " +
						"รายละเอียด [" + line.getM_AttributeSetInstance_ID()+ " " + line.getM_AttributeSetInstance().getDescription()+ "]\n");
			}
			JOptionPane.showMessageDialog(null, msg);
		}
		return true;
	}
	private boolean duplicateInventoryLine(MInventory inventory, MInventoryLine newLine){
		boolean dup = true;
		for(MInventoryLine mInventoryLine : inventory.getLines(true)){
			if(mInventoryLine.getM_Product_ID() == newLine.getM_Product_ID() && 
					mInventoryLine.getM_AttributeSetInstance_ID() == newLine.getM_AttributeSetInstance_ID()){
				return dup;
			}
		}
		return false;
	}

	protected Vector<String> getBOMColumnNames() {
		Vector<String> columnNames = new Vector<String>(8);
		columnNames.add(Msg.getMsg(Env.getCtx(), "เลือก"));
		columnNames.add(Msg.translate(Env.getCtx(), "ชื่อวัตถุดิบ"));
		columnNames.add(Msg.translate(Env.getCtx(), "คำอธิบาย"));
		columnNames.add(Msg.translate(Env.getCtx(), "รายละเอียด"));
		columnNames.add("จำนวนต่อตัว");
		columnNames.add("จำนวนเบิก");
		columnNames.add("จำนวนคงเหลือ");
		columnNames.add("หน่วย");
		return columnNames;
	}

	protected void configureMiniTable(IMiniTable miniTable) {
		miniTable.setColumnClass(0, Boolean.class, false); 		// 0-Selection
		miniTable.setColumnClass(1, String.class, true); 		// 1-Product
		miniTable.setColumnClass(2, String.class, true); 		// 2-BomLine Description
		miniTable.setColumnClass(3, String.class, true); 		// 3-AttributeSetInstance_ID
		miniTable.setColumnClass(4, BigDecimal.class, true); 	// 4-Qty
		miniTable.setColumnClass(5, BigDecimal.class, true); 	// 5-Qty Req.
		miniTable.setColumnClass(6, BigDecimal.class, true); 	// 6-Qty Available.
		miniTable.setColumnClass(7, String.class, true); 		// 7-UOM
		miniTable.autoSize();
	
		
		
	}

	protected Vector<Vector<Object>> getBOMDataByBomID(int PP_Product_BOM_ID) {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		System.out.println(PP_Product_BOM_ID);

		MPPProductBOM productBOM = new MPPProductBOM(Env.getCtx(),
				PP_Product_BOM_ID, null);
		System.out.println(productBOM.getName());

		MPPProductBOMLine[] bomLines = productBOM.getLines();
		for (int i = 0; i < bomLines.length; i++) {
			MPPProductBOMLine bomLine = bomLines[i];
			MProduct product = (MProduct) bomLine.getM_Product();
			
			int M_Locator_ID = product.getM_Locator_ID();
			int M_Product_ID = product.getM_Product_ID();
			int M_AttributeSetInstance_ID = bomLine.getM_AttributeSetInstance_ID();
			
			ArrayList<Object> params = new ArrayList<Object>();
			StringBuffer whereClause = new StringBuffer();
			
			String tableName = "M_Storage";
			whereClause.append(" M_Locator_ID = ? ");
			params.add(M_Locator_ID);
			whereClause.append(" and M_Product_ID = ? ");
			params.add(M_Product_ID);
			whereClause.append(" and M_AttributeSetInstance_ID = ? ");
			params.add(M_AttributeSetInstance_ID);
			Properties ctx = Env.getCtx();
			Query query = new Query(ctx, tableName, whereClause.toString(), null).setParameters(params);
			MStorage mStorage = query.first();
			double qtyAvailable = 0;
			if(mStorage != null){
				qtyAvailable = mStorage.getQtyOnHand().doubleValue() -  mStorage.getQtyReserved().doubleValue();
			}
			double qtyReq = bomLine.getQtyRequired().doubleValue();
			
			
			System.out.println(product.getName());
			Vector<Object> line = new Vector<Object>();
			// 0-Selection
			
			line.add(new Boolean(false));
			
			KeyNamePair pp = new KeyNamePair(product.get_ID(),
					product.getName());

			// 1-Product
			line.add(pp);
			//2-BomLine Description
			line.add(bomLine.getDescription());
			//3-AttributeSetInstance_ID
			
			
			Query qry = new Query(ctx, "M_AttributeSetInstance", "M_AttributeSetInstance_ID = " + M_AttributeSetInstance_ID , null);
			MAttributeSetInstance mAttSetInstance = qry.first();
			String M_AttSetIns_Desc = mAttSetInstance.getDescription();
			KeyNamePair attSetIns = new KeyNamePair(M_AttributeSetInstance_ID, M_AttSetIns_Desc);
			line.add(attSetIns);
			
			// 4-Qty PP_Product_BOMLine.QtyBOM
			line.add(bomLine.getQty());
			// 5-Qty Req. PP_Product_BOMLine.QtyRequired
			line.add(bomLine.getQtyRequired());
			// 6-Qty Available. 
			
			
			
			
			line.add(new BigDecimal(qtyAvailable));
			// 7-UOM
			line.add(product.getUOMSymbol());
			data.add(line);
		}

		return data;
	}
	
	protected Vector<Vector<Object>> getBOMData(int M_Product_ID) {
		/*
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
//		String sql = "select pp_product_bom_id from pp_product_bom where isbom_mrq = 'Y' and m_product_id = " + M_Product_ID;
		String sql = 	"select pp_product_bom_id from pp_product_bom " +
						"where 	isbom_mrq = 'Y' and isactive = 'Y' " +
								"and m_product_id = "+M_Product_ID+
								" and updated = ( select max(updated) " +
												"from pp_product_bom " +
												"where 	isbom_mrq = 'Y' and isactive = 'Y' " +
														"and m_product_id = "+M_Product_ID+" )";
		log.info("========");
		log.info(sql);
		log.info("========");
		
		int PP_Product_BOM_ID = DB.getSQLValue(null, sql);
		System.out.println(PP_Product_BOM_ID);

		MPPProductBOM productBOM = new MPPProductBOM(Env.getCtx(),
				PP_Product_BOM_ID, null);
		System.out.println(productBOM.getName());

		MPPProductBOMLine[] bomLines = productBOM.getLines();
		for (int i = 0; i < bomLines.length; i++) {
			MPPProductBOMLine bomLine = bomLines[i];
			MProduct product = (MProduct) bomLine.getM_Product();
			System.out.println(product.getName());
			Vector<Object> line = new Vector<Object>();
			// 0-Selection
			line.add(new Boolean(false));
			KeyNamePair pp = new KeyNamePair(product.get_ID(),
					product.getName());
			// 1-Product
			line.add(pp);
			// 2-Qty
			line.add(new BigDecimal(0));
			// 3-Qty Req.
			line.add(new BigDecimal(0));
			// 4-UOM
			line.add(product.getUOMSymbol());
			data.add(line);
		}

		return data;
		*/
		return null;
	}
	
}
