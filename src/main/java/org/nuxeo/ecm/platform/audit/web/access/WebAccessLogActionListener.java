/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     anguenot
 *
 * $Id: WebAccessLogActionListener.java 28475 2008-01-04 09:49:02Z sfermigier $
 */

package org.nuxeo.ecm.platform.audit.web.access;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEvent;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventCategories;
import org.nuxeo.ecm.core.api.event.impl.CoreEventImpl;
import org.nuxeo.ecm.platform.audit.web.access.api.AccessLogObserver;
import org.nuxeo.ecm.platform.audit.web.access.api.WebAccessConstants;
import org.nuxeo.ecm.platform.audit.web.access.api.local.AccessLogObserverLocal;
import org.nuxeo.ecm.platform.audit.web.access.api.remote.AccessLogObserverRemote;
import org.nuxeo.ecm.platform.events.api.DocumentMessageProducer;
import org.nuxeo.ecm.platform.events.api.DocumentMessageProducerException;
import org.nuxeo.ecm.platform.events.api.EventMessage;
import org.nuxeo.ecm.platform.events.api.delegate.DocumentMessageProducerBusinessDelegate;
import org.nuxeo.ecm.platform.events.api.impl.DocumentMessageImpl;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * Access log action listener.
 *
 * <p>
 * Responsible of logging web access. Attention, this will decrease the
 * performances of the webapp.
 * </p>
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 *
 */
@Stateful
@Local(AccessLogObserverLocal.class)
@Remote(AccessLogObserverRemote.class)
@Name("webAccessLogObserver")
@Scope(CONVERSATION)
@SerializedConcurrentAccess
public class WebAccessLogActionListener implements AccessLogObserver {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(WebAccessLogActionListener.class);

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true)
    protected Principal currentUser;

    protected transient DocumentMessageProducer producer;

    @Destroy
    @Remove
    public void destroy() {
        log.debug("Removing SEAM component...");
    }

    protected DocumentMessageProducer getProducer()
            throws DocumentMessageProducerException {
        if (producer == null) {
            producer = DocumentMessageProducerBusinessDelegate.getRemoteDocumentMessageProducer();
        }
        return producer;
    }

    @Observer( { EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED })
    public void log() throws DocumentMessageProducerException {
        DocumentMessageProducer producer = getProducer();
        DocumentModel dm = navigationContext.getCurrentDocument();
        Map<String, Serializable> props = new HashMap<String, Serializable>();
        try {
            props.put(CoreEventConstants.DOC_LIFE_CYCLE,
                    dm.getCurrentLifeCycleState());
        } catch (ClientException ce) {
            throw new DocumentMessageProducerException(ce.getMessage(), ce);
        }
        CoreEvent event = new CoreEventImpl(WebAccessConstants.EVENT_ID, dm,
                props, currentUser,
                DocumentEventCategories.EVENT_DOCUMENT_CATEGORY, null);
        EventMessage msg = new DocumentMessageImpl(dm, event);
        producer.produce(msg);
    }
}
