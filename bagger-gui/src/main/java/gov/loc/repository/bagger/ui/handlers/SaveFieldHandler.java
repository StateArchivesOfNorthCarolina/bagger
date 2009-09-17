
package gov.loc.repository.bagger.ui.handlers;

import gov.loc.repository.bagger.bag.impl.DefaultBag;
import gov.loc.repository.bagger.ui.BagView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SaveFieldHandler extends AbstractAction {
	private static final Log log = LogFactory.getLog(SaveFieldHandler.class);
   	private static final long serialVersionUID = 1L;
	BagView bagView;
	DefaultBag bag;

	public SaveFieldHandler(BagView bagView) {
		super();
		this.bagView = bagView;
	}

	public void actionPerformed(ActionEvent e) {
    	bagView.infoInputPane.updateBagHandler.updateBag(bagView.getBag());
		bagView.saveProfiles();
		bagView.bagInfoInputPane.setSelectedIndex(1);
	}
}