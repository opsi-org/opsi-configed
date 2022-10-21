package de.uib.opsidatamodel.datachanges;

interface UpdateCommand
{
	void doCall();
	Object getController( );
	void setController( Object cont);
}

