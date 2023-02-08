package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;

public class ProductpropertiesUpdate implements UpdateCommand {
	String pcname;
	String productname;
	Map newdata;

	AbstractPersistenceController persis;

	public ProductpropertiesUpdate(AbstractPersistenceController persis, String pcname, String productname,
			Map newdata) {
		this.pcname = pcname;
		this.productname = productname;
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (AbstractPersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}

	@Override
	public void doCall() {

		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {

			persis.setProductproperties(pcname, productname, newdata);
		}
	}

	public void revert() {
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {

			((de.uib.configed.type.ConfigName2ConfigValue) newdata).rebuild();

		}
	}

}
