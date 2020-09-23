package org.compiere.grid;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.compiere.apps.AEnv;
import org.compiere.grid.ed.VLookup;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MDocType;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.eevolution.model.X_PP_Product_BOM;
	
import org.compiere.model.X_M_Locator;   /* Aon 7/3/2017 */

/**
 * 
 * @author noum   29/09/2012 16:05, prapon 12/2016,18/1/2017
 * 
 */

public class VCreateFromInternalUseUI extends VCreateFromInternalUse implements
		ActionListener, VetoableChangeListener {
	private String tag = "VCreateFromInternalUseUI";
	/** Logger */
	private CLogger log = CLogger.getCLogger(getClass());

	private VCreateFromDialog dialog;
	private boolean m_actionActive = false;
	/** Window No */
	private int p_WindowNo;
	private int M_Warehouse_ID;
	private int M_Locator_ID;
	private int PP_Product_BOM_ID;
	
	public VCreateFromInternalUseUI(GridTab mTab) {
		super(mTab);
		
		// TODO Auto-generated constructor stub
		M_Warehouse_ID = (Integer)mTab.getField("M_Warehouse_ID").getValue();
		M_Locator_ID = (Integer)mTab.getField("M_Locator_ID").getValue();  // Aon 01/02/2017
		PP_Product_BOM_ID = (Integer)mTab.getField("PP_Product_BOM_ID").getValue();
			
		log.info(tag + "Warehouse " + M_Warehouse_ID + ", Locator " + M_Locator_ID + ", BOM " + "PP_Product_BOM_ID :" + PP_Product_BOM_ID);

		dialog = new VCreateFromDialog(this, getGridTab().getWindowNo(), true);
		p_WindowNo = getGridTab().getWindowNo();
		try {
			if (!dynInit())
				return;
			jbInit();
			loadBOMbyBomId(PP_Product_BOM_ID);
			setInitOK(true);
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
			setInitOK(false);
		}
		AEnv.positionCenterWindow(Env.getWindow(p_WindowNo), dialog);
		log.info("hi");
	}

	/**
	 * Dynamic Init
	 * 
	 * @throws Exception
	 *             if Lookups cannot be initialized
	 * @return true if initialized
	 */
	public boolean dynInit() throws Exception {
		log.config("");

		super.dynInit();

		dialog.setTitle(getTitle());

		// initBPartner(true);
		initProduct();
		fldProduct.addVetoableChangeListener(this);
		fldProduct.setEnabled(false);
		
		return true;
	} // dynInit

	private JLabel lblProduct = new JLabel();
	private VLookup fldProduct;
	private JLabel txtProduct = new JLabel();

	private void jbInit() throws Exception {
		Properties ctx = Env.getCtx();
		X_PP_Product_BOM bom = new X_PP_Product_BOM(ctx, PP_Product_BOM_ID, null);
		String bomName = bom.getName();
		// String bomDesc = (bom.getDescription()==null)?"":" [ "+bom.getDescription()+" ]"; Aon 7/3/2017

		X_M_Locator loc = new X_M_Locator(ctx, M_Locator_ID, null);
		String locName = loc.getValue();
		
		
		lblProduct.setText(/*Msg.getElement(Env.getCtx(), "M_Product_ID") + */" (รุ่นงาน) :");
		txtProduct.setText(bomName/* + bomDesc*/+" , Locator : "+locName+ " ("+M_Locator_ID+") Sort by ComponentType+Line");
		
		CPanel parameterPanel = dialog.getParameterPanel();
		parameterPanel.setLayout(new BorderLayout());

		CPanel parameterStdPanel = new CPanel(new GridBagLayout());

		parameterPanel.add(parameterStdPanel, BorderLayout.CENTER);

		parameterStdPanel.add(lblProduct, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
//		if (fldProduct != null)
//			parameterStdPanel.add(fldProduct,
//					new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//							GridBagConstraints.WEST,
//							GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5,
//									5), 0, 0));
		
			parameterStdPanel.add(txtProduct,
					new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.WEST,
							GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5,
									5), 0, 0));

	} // jbInit

	protected void initProduct() throws Exception {
		int AD_Column_ID = 1402; // M_Product.M_Product_ID
		MLookup lookup = MLookupFactory.get(Env.getCtx(), p_WindowNo, 0,
				AD_Column_ID, DisplayType.Search);
		fldProduct = new VLookup("M_Product_ID", true, false, true, lookup);
		// fldProduct.addActionListener(this);
	}

	protected void loadBOM(int M_Product_ID){
		loadProductBOM(getBOMData(M_Product_ID));
	}
	protected void loadBOMbyBomId(int PP_Product_BOM_ID){
		loadProductBOM(getBOMDataByBomID(PP_Product_BOM_ID, M_Warehouse_ID, M_Locator_ID));
	}
	protected void loadProductBOM(Vector<?> data) {

		// Remove previous listeners
		dialog.getMiniTable().getModel().removeTableModelListener(dialog);
		// Set Model
		DefaultTableModel model = new DefaultTableModel(data,getBOMColumnNames());
		model.addTableModelListener(dialog);
		dialog.getMiniTable().setModel(model);
		//

		configureMiniTable(dialog.getMiniTable());		
	}
	

	@Override
	public void vetoableChange(PropertyChangeEvent e)
			throws PropertyVetoException {
		// TODO Auto-generated method stub
		log.info("source : " + e.getSource());
		log.info("property name : " + e.getPropertyName());

		log.info("old value : " + e.getOldValue());
		log.info("new value : " + e.getNewValue());
		log.config(e.getPropertyName() + "=" + e.getNewValue());
		try{
		// BPartner - load Order/Invoice/Shipment
		if (e.getPropertyName().equals("M_Product_ID")) {
			int M_Product_ID = ((Integer) e.getNewValue()).intValue();
			log.info("select M_Product_ID : " + M_Product_ID);
			loadBOM(M_Product_ID);
		}
		}catch(Exception ex){
			throw new PropertyVetoException("load BOM error", e);
		}
		dialog.tableChanged(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		log.config("Action=" + e.getActionCommand());
		log.info("source : " + e.getSource());
		log.info("Action : " + e.getActionCommand());
		if (m_actionActive)
			return;
		m_actionActive = true;
		log.config("Action=" + e.getActionCommand());
		// Product
		// if (e.getSource().equals(fldProduct)) {
		// System.out.println("product selected");
		// }
	}

	public void showWindow() {
		dialog.setVisible(true);
	}

	public void closeWindow() {
		dialog.dispose();
	}

}