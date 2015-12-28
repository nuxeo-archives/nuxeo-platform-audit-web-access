/*
 * (C) Copyright 2006-2007 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     anguenot
 *
 * $Id: WebAccessLogActionListener.java 28475 2008-01-04 09:49:02Z sfermigier $
 */

package org.nuxeo.ecm.platform.audit.web.access;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventCategories;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.audit.web.access.api.AccessLogObserver;
import org.nuxeo.ecm.platform.audit.web.access.api.WebAccessConstants;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;

/**
 * Access log action listener.
 * <p>
 * Responsible of logging web access. Attention, this will decrease the performances of the webapp.
 * </p>
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 * @author <a href="mailto:vdutat@yahoo.fr">Vincent Dutat</a>
 */
@Name("webAccessLogObserver")
@Scope(CONVERSATION)
public class WebAccessLogActionListener implements AccessLogObserver {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(WebAccessLogActionListener.class);

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true)
    protected Principal currentUser;

    @Observer(value = { EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED }, create = true)
    public void log() {
        DocumentModel dm = navigationContext.getCurrentDocument();
        if (dm == null) {
            return;
        }
        DocumentEventContext ctx = new DocumentEventContext(navigationContext.getCurrentDocument().getCoreSession(),
                currentUser, dm);
        ctx.setCategory(DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);
        ctx.setProperty(CoreEventConstants.DOC_LIFE_CYCLE, dm.getCurrentLifeCycleState());
        ctx.setProperty(CoreEventConstants.SESSION_ID, dm.getSessionId());
        Event event = ctx.newEvent(WebAccessConstants.EVENT_ID);
        EventService evtService = Framework.getService(EventService.class);
        log.debug("Sending scheduled event id=" + WebAccessConstants.EVENT_ID + ", category="
                + DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);
        evtService.fireEvent(event);
    }
}
