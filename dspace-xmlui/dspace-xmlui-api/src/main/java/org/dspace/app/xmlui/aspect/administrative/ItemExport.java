/*
 * ItemExport.java
 *
 * Version: $Revision: 1.3 $
 *
 * Date: $Date: 2006/07/13 23:20:54 $
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.app.xmlui.aspect.administrative;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.eperson.Group;
import org.xml.sax.SAXException;

/**
 * 
 * Create the ability to view currently available export archives for download.
 * 
 * @author Jay Paz
 */
public class ItemExport extends AbstractDSpaceTransformer implements
		CacheableProcessingComponent {
	private final static Message T_dspace_home = message("xmlui.general.dspace_home");

	private static final Message T_main_head = message("xmlui.administrative.ItemExport.head");

	private static final Message T_export_bad_item_id = message("xmlui.administrative.ItemExport.item.id.error");

	private static final Message T_export_bad_col_id = message("xmlui.administrative.ItemExport.collection.id.error");

	private static final Message T_export_bad_community_id = message("xmlui.administrative.ItemExport.community.id.error");

	private static final Message T_export_item_not_found = message("xmlui.administrative.ItemExport.item.not.found");

	private static final Message T_export_col_not_found = message("xmlui.administrative.ItemExport.collection.not.found");

	private static final Message T_export_community_not_found = message("xmlui.administrative.ItemExport.community.not.found");

	private static final Message T_item_export_success = message("xmlui.administrative.ItemExport.item.success");

	private static final Message T_col_export_success = message("xmlui.administrative.ItemExport.collection.success");

	private static final Message T_community_export_success = message("xmlui.administrative.ItemExport.community.success");

	private static final Message T_avail_head = message("xmlui.administrative.ItemExport.available.head");

	/** The Cocoon request */
	Request request;

	/** The Cocoon response */
	Response response;

	java.util.List<Message> errors;

	java.util.List<String> availableExports;

	Message message;

	/** Cached validity object */
	private SourceValidity validity;

	@Override
	public void setup(SourceResolver resolver, Map objectModel, String src,
			Parameters parameters) throws ProcessingException, SAXException,
			IOException {
		super.setup(resolver, objectModel, src, parameters);
		this.objectModel = objectModel;
		this.request = ObjectModelHelper.getRequest(objectModel);
		this.response = ObjectModelHelper.getResponse(objectModel);
		try {
			availableExports = org.dspace.app.itemexport.ItemExport
					.getExportsAvailable(context.getCurrentUser());
		} catch (Exception e) {
			// nothing to do
		}
		errors = new ArrayList<Message>();
		if (request.getParameter("itemID") != null) {
			Item item = null;
			try {
				item = Item.find(context, Integer.parseInt(request
						.getParameter("itemID")));
			} catch (Exception e) {
				errors.add(T_export_bad_item_id);
			}

			if (item == null) {
				errors.add(T_export_item_not_found);
			} else {
				try {
					org.dspace.app.itemexport.ItemExport
							.createDownloadableExport(item, context);
				} catch (Exception e) {
					errors.add(message(e.getMessage()));
				}
			}
			if (errors.size() <= 0)
				message = T_item_export_success;
		} else if (request.getParameter("collectionID") != null) {
			Collection col = null;
			try {
				col = Collection.find(context, Integer.parseInt(request
						.getParameter("collectionID")));
			} catch (Exception e) {
				errors.add(T_export_bad_col_id);
			}

			if (col == null) {
				errors.add(T_export_col_not_found);
			} else {
				try {
					org.dspace.app.itemexport.ItemExport
							.createDownloadableExport(col, context);
				} catch (Exception e) {
					errors.add(message(e.getMessage()));
				}
			}
			if (errors.size() <= 0)
				message = T_col_export_success;
		} else if (request.getParameter("communityID") != null) {
			Community com = null;
			try {
				com = Community.find(context, Integer.parseInt(request
						.getParameter("communityID")));
			} catch (Exception e) {
				errors.add(T_export_bad_community_id);
			}

			if (com == null) {
				errors.add(T_export_community_not_found);
			} else {
				try {
					org.dspace.app.itemexport.ItemExport
							.createDownloadableExport(com, context);
				} catch (Exception e) {
					errors.add(message(e.getMessage()));
				}
			}
			if (errors.size() <= 0)
				message = T_community_export_success;
		}
	}

	/**
	 * Generate the unique cache key.
	 * 
	 * @return The generated key hashes the src
	 */
	public Serializable getKey() {
		Request request = ObjectModelHelper.getRequest(objectModel);

		// Special case, don't cache anything if the user is logging
		// in. The problem occures because of timming, this cache key
		// is generated before we know whether the operation has
		// succeded or failed. So we don't know whether to cache this
		// under the user's specific cache or under the anonymous user.
		if (request.getParameter("login_email") != null
				|| request.getParameter("login_password") != null
				|| request.getParameter("login_realm") != null) {
			return "0";
		}

		String key;
		if (context.getCurrentUser() != null) {
			key = context.getCurrentUser().getEmail();
			if (availableExports != null && availableExports.size() > 0) {
				for (String fileName : availableExports) {
					key += ":" + fileName;
				}
			}

			if (request.getQueryString() != null) {
				key += request.getQueryString();
			}
		} else
			key = "anonymous";

		return HashUtil.hash(key);
	}

	/**
	 * Generate the validity object.
	 * 
	 * @return The generated validity object or <code>null</code> if the
	 *         component is currently not cacheable.
	 */
	public SourceValidity getValidity() {
		if (this.validity == null) {
			// Only use the DSpaceValidity object is someone is logged in.
			if (context.getCurrentUser() != null) {
				try {
					DSpaceValidity validity = new DSpaceValidity();

					validity.add(eperson);

					Group[] groups = Group.allMemberGroups(context, eperson);
					for (Group group : groups) {
						validity.add(group);
					}

					this.validity = validity.complete();
				} catch (SQLException sqle) {
					// Just ignore it and return invalid.
				}
			} else {
				this.validity = NOPValidity.SHARED_INSTANCE;
			}
		}
		return this.validity;
	}

	/**
	 * Add Page metadata.
	 */
	public void addPageMeta(PageMeta pageMeta) throws SAXException,
			WingException, UIException, SQLException, IOException,
			AuthorizeException {
		pageMeta.addMetadata("title").addContent(T_main_head);

		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		pageMeta.addTrail().addContent(T_main_head);
	}

	public void addBody(Body body) throws SAXException, WingException,
			UIException, SQLException, IOException, AuthorizeException {
		Division main = body.addDivision("export_main");
		main.setHead(T_main_head);

		if (message != null) {
			main.addDivision("success", "success").addPara(message);
		}

		if (errors.size() > 0) {
			Division errors = main.addDivision("export-errors", "error");
			for (Message error : this.errors) {
				errors.addPara(error);
			}
		}

		if (availableExports != null && availableExports.size() > 0) {
			Division avail = main.addDivision("available-exports",
					"available-exports");
			avail.setHead(T_avail_head);

			List fileList = avail.addList("available-files", List.TYPE_ORDERED);
			for (String fileName : availableExports) {
				fileList.addItem().addXref(
						this.contextPath + "/exportdownload/" + fileName,
						fileName);
			}
		}
	}

	/**
	 * recycle
	 */
	public void recycle() {
		this.validity = null;
		this.errors = null;
		this.message = null;
		this.availableExports = null;
		super.recycle();
	}

}
