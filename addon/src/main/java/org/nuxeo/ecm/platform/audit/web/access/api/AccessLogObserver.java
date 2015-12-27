/*
 * (C) Copyright 2006-2007 Nuxeo SA (http://nuxeo.com/) and others.
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
 * $Id: AccessLogObserver.java 28475 2008-01-04 09:49:02Z sfermigier $
 */

package org.nuxeo.ecm.platform.audit.web.access.api;

import java.io.Serializable;

/**
 * Interface for access log observer.
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 */
public interface AccessLogObserver extends Serializable {

    /**
     * Create one log.
     *
     * @throws DocumentMessageProducerException
     */
    void log();

}
