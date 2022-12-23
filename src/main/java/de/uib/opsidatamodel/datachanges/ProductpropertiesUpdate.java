package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;

public class ProductpropertiesUpdate implements UpdateCommand {
	String pcname;
	String productname;
	Map newdata;

	PersistenceController persis;

	public ProductpropertiesUpdate(PersistenceController persis, String pcname, String productname, Map newdata) {
		this.pcname = pcname;
		this.productname = productname;
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (PersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}

	@Override
	public void doCall() {
		// if (newdata != null) logging.debug(this, "doCall, setting " + newdata + ",
		
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {
			// logging.info(this, "doCall, set " + newdata + " , we retrieved: " +
			

			persis.setProductproperties(pcname, productname, newdata);
		}
	}

	public void revert() {
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {
			
			((de.uib.configed.type.ConfigName2ConfigValue) newdata).rebuild();

			
		}
	}

}
