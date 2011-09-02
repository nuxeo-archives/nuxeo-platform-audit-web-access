package org.nuxeo.ecm.platform.audit.web.access;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.platform.audit.web.access.api.WebAccessConstants;
import org.nuxeo.ecm.platform.ui.web.directory.DirectoryHelper;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class AuditWebAccessComponent extends DefaultComponent implements
        FrameworkListener {

    private static final Log log = LogFactory.getLog(AuditWebAccessComponent.class);

    @Override
    public void activate(ComponentContext context) throws Exception {
        context.getRuntimeContext().getBundle().getBundleContext().addFrameworkListener(
                this);
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        // this is doing nothing if listener was not registered
        context.getRuntimeContext().getBundle().getBundleContext().removeFrameworkListener(
                this);
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTED) {
            Session dirSession = null;
            try {
                if (DirectoryHelper.getDirectoryService().getDirectory(
                        WebAccessConstants.DIRECTORY_ID) == null) {
                    return;
                }
                dirSession = DirectoryHelper.getDirectoryService().open(
                        WebAccessConstants.DIRECTORY_ID);
                if (!dirSession.hasEntry(WebAccessConstants.EVENT_ID)) {
                    Map<String, Object> fieldMap = new HashMap<String, Object>();
                    fieldMap.put("id", WebAccessConstants.EVENT_ID);
                    fieldMap.put("label", WebAccessConstants.EVENT_ID);
                    fieldMap.put("obsolete", 0);
                    fieldMap.put("ordering", 500);
                    dirSession.createEntry(fieldMap);
                }
            } catch (DirectoryException e) {
                log.error("Cannot open directory"
                        + WebAccessConstants.DIRECTORY_ID);
            } catch (ClientException e) {
                log.error("Cannot create entry" + WebAccessConstants.EVENT_ID
                        + " in directory" + WebAccessConstants.DIRECTORY_ID);
            } finally {
                if (dirSession != null) {
                    try {
                        dirSession.close();
                    } catch (DirectoryException e) {
                        log.error("Cannot close session");
                    }
                }
            }
        }

    }

}
