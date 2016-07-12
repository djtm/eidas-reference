/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.stork.peps;

import eu.stork.peps.auth.commons.exceptions.AbstractPEPSException;
import eu.stork.peps.auth.commons.exceptions.CPEPSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles the all the {@link Exception} and the
 * eu.stork.peps.exceptions.PEPSInterceptorException.
 */
public final class InternalExceptionHandlerServlet extends AbstractPepsServlet {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 7925862066060762369L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InternalExceptionHandlerServlet.class.getName());

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleError(request, response);
    }

    /**
     * Prepares the exception to be displayed by the correct view.
     *
     * @param request
     * @param response
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response) {
      /**
       * Current exception.
       */
      Exception exception;

      //Default error page
      String retVal = PepsViewNames.INTERNAL_ERROR.toString();
        try {
            // Prevent cookies from being accessed through client-side script.
            setHTTPOnlyHeaderToSession(false, request, response);

            // Analyze the servlet exception
            exception = (Exception) request.getAttribute("javax.servlet.error.exception");

            //Websphere wraps another layer onto the thrown exception
            if(exception instanceof ServletException && exception.getCause() instanceof ServletException) {
                exception = (Exception) exception.getCause();
            }
            if (exception.getCause() instanceof AbstractPEPSException) {
                LOG.info("ERROR : An error occurred on PEPS (no. {}) - {}", ((AbstractPEPSException) exception.getCause()).getErrorCode(), ((AbstractPEPSException) exception.getCause()).getErrorMessage());
                LOG.debug("ERROR : Exception Stacktrace", exception);
            } else {
                LOG.info("ERROR : An error occurred on PEPS", exception.getMessage());
                LOG.debug("ERROR : An error occurred on PEPS", exception);
            }

            if (exception.getCause() instanceof CPEPSException) {
                LOG.trace("Exception is instanceOf CPEPSException");
                retVal = "/CPEPSExceptionHandler";
                //Restore the exception with the original because filters
                //may have thrown it as ServletException
                exception = (Exception) exception.getCause();
                request.setAttribute("javax.servlet.error.exception", exception);
            }else if (exception.getCause() instanceof AbstractPEPSException) {
                LOG.trace("Exception is instanceOf AbstractPEPSException");
                retVal = "/StorkPEPSExceptionHandler";
                //Restore the exception with the original because filters
                //may have thrown it as ServletException
                exception = (Exception) exception.getCause();
                request.setAttribute("javax.servlet.error.exception", exception);
            } else {
                // Default General error
                LOG.info("ERROR : Exception occurs (NOT instanceOf PEPSInterceptorException {})", exception.getMessage());
                LOG.debug("ERROR : Exception occurs (NOT instanceOf PEPSInterceptorException {})", exception);
            }

            //Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code"); NEVER used
            request.setAttribute(PepsBeanNames.EXCEPTION.toString(), exception);

            //Forward to error page
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(retVal);
            dispatcher.forward(request, response);
        } catch (Exception e) {
            LOG.info("ERROR : Exception occurs {}", e.getMessage());
            LOG.debug("ERROR : Exception occurs {}", e);
        }
    }
}