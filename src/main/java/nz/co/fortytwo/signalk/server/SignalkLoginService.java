/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is part of the signalk-server-java project
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
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
 */
package nz.co.fortytwo.signalk.server;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;

public class SignalkLoginService implements LoginService {
	    private final static Logger log = LogManager.getLogger(SignalkLoginService.class);

	    private final Map<String, Boolean> users = new ConcurrentHashMap<>();

	    // matches what is in the constraint object in the spring config
	    private final String[] ACCESS_ROLE = new String[] { "rolename" };
		
	    private IdentityService identityService = new DefaultIdentityService();

		@Override
		public IdentityService getIdentityService() {
			return identityService;
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public UserIdentity login(String username, Object creds) {
			
	        UserIdentity user = null;
	        
	        
	        // HERE IS THE HARDCODING
			boolean validUser = "admin".equals(username) && "s3cr3t".equals(creds);
			if (validUser) {
				Credential credential = (creds instanceof Credential)?(Credential)creds:Credential.getCredential(creds.toString());

			    Principal userPrincipal = new MappedLoginService.KnownUser(username,credential);
			    Subject subject = new Subject();
			    subject.getPrincipals().add(userPrincipal);
			    subject.getPrivateCredentials().add(creds);
			    subject.setReadOnly();
			    user=identityService.newUserIdentity(subject,userPrincipal, ACCESS_ROLE);
			    users.put(user.getUserPrincipal().getName(), true);
			}

		    return (user != null) ? user : null;
		}

		@Override
		public void logout(UserIdentity arg0) {
			
		}

		@Override
		public void setIdentityService(IdentityService arg0) {
		     this.identityService = arg0;
			
		}

		@Override
		public boolean validate(UserIdentity user) {
			if (users.containsKey(user.getUserPrincipal().getName()))
	            return true;

	        return false;	
		}
	

}
