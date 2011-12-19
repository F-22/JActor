/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki.jactor.lpc;

import org.agilewiki.jactor.apc.*;

/**
 * Implements LPCActor.
 */
abstract public class JLPCActor implements LPCActor {

    /**
     * The inbox and outbox of the actor.
     */
    private LPCMailbox mailbox;

    /**
     * Handles callbacks from the mailbox.
     */
    private APCRequestProcessor requestProcessor = new APCRequestProcessor() {
        private ExceptionHandler exceptionHandler;

        public ExceptionHandler getExceptionHandler() {
            return exceptionHandler;
        }

        public void setExceptionHandler(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public void haveEvents() {
            mailbox.dispatchEvents();
        }

        @Override
        public void processRequest(JAPCRequest request) throws Exception {
            JLPCActor.this.processRequest(request.getUnwrappedRequest(), new ResponseProcessor() {
                @Override
                public void process(Object unwrappedResponse) {
                    mailbox.response(unwrappedResponse);
                }
            });
        }
    };

    /**
     * Wraps and enqueues an unwrapped request in the requester's outbox.
     *
     * @param requestSource    The originator of the request.
     * @param unwrappedRequest The unwrapped request to be sent.
     * @param rd               The request processor.
     */
    @Override
    public void acceptRequest(APCRequestSource requestSource, Object unwrappedRequest, ResponseProcessor rd) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Set the initial capacity for buffered outgoing messages.
     *
     * @param initialBufferCapacity The initial capacity for buffered outgoing messages.
     */
    @Override
    public void setInitialBufferCapacity(int initialBufferCapacity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Call a function repeatedly until the result is not null.
     *
     * @param function          Provides the function to be called.
     * @param responseProcessor Processes the final, non-null result.
     * @throws Exception Any uncaught exceptions raised when calling the provided function.
     */
    final protected void iterate(final Function function,
                                 final ResponseProcessor responseProcessor) throws Exception {
        final MutableBoolean sync = new MutableBoolean();
        final MutableBoolean async = new MutableBoolean();
        sync.value = true;
        while (sync.value) {
            sync.value = false;
            ResponseProcessor rd = new ResponseProcessor() {
                @Override
                public void process(Object unwrappedResponse) throws Exception {
                    if (unwrappedResponse == null) {
                        if (!async.value) {
                            sync.value = true;
                        } else {
                            iterate(function, responseProcessor); //not recursive
                        }
                    } else responseProcessor.process(unwrappedResponse);
                }
            };
            function.process(rd);
            if (!sync.value) {
                async.value = true;
            }
        }
    }

    /**
     * The application method for processing requests sent to the actor.
     *
     * @param unwrappedRequest  An unwrapped request.
     * @param responseProcessor The response processor.
     * @throws Exception Any uncaught exceptions raised while processing the request.
     */
    abstract protected void processRequest(Object unwrappedRequest, ResponseProcessor responseProcessor)
            throws Exception;
}
