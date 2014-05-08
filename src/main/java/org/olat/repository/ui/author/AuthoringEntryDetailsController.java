/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.RepositoyUIFactory;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Price;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Initial date: 29.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEntryDetailsController extends FormBasicController {
	
	private FormLink markLink, startLink;
	private Link editLink;
	
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private AuthoringEditEntryController editCtrl;
	
	private final TooledStackedPanel stackPanel;
	
	private final RepositoryEntry entry;
	private final AuthoringEntryRow row;

	private final ACService acService;
	private final MarkManager markManager;
	private final CatalogManager catalogManager;
	private final RepositoryService repositoryService;
	private final BusinessGroupService businessGroupService;
	
	private String baseUrl;
	
	public AuthoringEntryDetailsController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, AuthoringEntryRow row) {
		super(ureq, wControl, "details");
		
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		acService = CoreSpringFactory.getImpl(ACService.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		this.stackPanel = stackPanel;
		this.row = row;
		entry = repositoryService.loadByKey(row.getKey());
		
		initForm(ureq);
		
		if(stackPanel != null) {
			String displayName = row.getDisplayname();
			stackPanel.pushController(displayName, this);
			
			editLink = LinkFactory.createToolLink("edit", "Edit", this);
			stackPanel.addTool(editLink, false);
		}
	}
	
	private void setText(String text, String key, FormLayoutContainer layoutCont) {
		if(!StringHelper.containsNonWhitespace(text)) return;
		text = StringHelper.xssScan(text);
		if(baseUrl != null) {
			text = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUrl).filter(text);
		}
		text = Formatter.formatLatexFormulas(text);
		layoutCont.contextPut(key, text);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int cmpcount = 0;
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("v", entry);
			String cssClass = RepositoyUIFactory.getIconCssClass(entry);
			layoutCont.contextPut("cssClass", cssClass);
			
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
			VFSContainer mediaContainer = handler.getMediaContainer(entry);
			if(mediaContainer != null) {
				baseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
			}
			
			setText(entry.getDescription(), "description", layoutCont);
			setText(entry.getRequirements(), "requirements", layoutCont);
			setText(entry.getObjectives(), "objectives", layoutCont);
			setText(entry.getCredits(), "credits", layoutCont);

			//thumbnail and movie
			VFSLeaf movie = repositoryService.getIntroductionMovie(entry);
			VFSLeaf image = repositoryService.getIntroductionImage(entry);
			if(image != null || movie != null) {
				ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
				if(movie != null) {
					ic.setMedia(movie);
					ic.setMaxWithAndHeightToFitWithin(500, 300);
				} else {
					ic.setMedia(image);
					ic.setMaxWithAndHeightToFitWithin(500, 300);
				}
				layoutCont.put("thumbnail", ic);
			}
			
			//categories
			List<CatalogEntry> categories = catalogManager.getCatalogEntriesReferencing(entry);
			List<String> categoriesLink = new ArrayList<>(categories.size());
			for(CatalogEntry category:categories) {
				String id = "cat_" + ++cmpcount;
				String title = category.getParent().getName();
				FormLink catLink = uifactory.addFormLink(id, "category", title, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
				catLink.setUserObject(category.getKey());
				categoriesLink.add(id);
			}
			layoutCont.contextPut("categories", categoriesLink);
			
			boolean marked;
			if(row == null) {
				marked = markManager.isMarked(entry, getIdentity(), null);
			} else {
				marked = row.isMarked();
			}
			markLink = uifactory.addFormLink("mark", "mark", "&nbsp;&nbsp;&nbsp;&nbsp;", null, layoutCont, Link.NONTRANSLATED);
			markLink.setCustomEnabledLinkCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			

			
			//load memberships
			boolean isMember = repositoryService.isMember(getIdentity(), entry);
			
			//access control
			List<PriceMethod> types = new ArrayList<PriceMethod>();
			if (entry.isMembersOnly()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "b_access_membersonly_icon"));
				if(isMember) {
					startLink = uifactory.addFormLink("start", "start", "start", null, layoutCont, Link.LINK);
				}
			} else {
				AccessResult acResult = acService.isAccessible(entry, getIdentity(), false);
				if(acResult.isAccessible()) {
					startLink = uifactory.addFormLink("start", "start", "start", null, layoutCont, Link.LINK);
				} else if (acResult.getAvailableMethods().size() > 0) {
					for(OfferAccess access:acResult.getAvailableMethods()) {
						AccessMethod method = access.getMethod();
						String type = (method.getMethodCssClass() + "_icon").intern();
						Price p = access.getOffer().getPrice();
						String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
						types.add(new PriceMethod(price, type));
					}
					startLink = uifactory.addFormLink("start", "start", "book", null, layoutCont, Link.LINK);
				} else {
					startLink = uifactory.addFormLink("start", "start", "start", null, layoutCont, Link.LINK);
					startLink.setEnabled(false);
				}
			}
			
			if(!types.isEmpty()) {
				layoutCont.contextPut("ac", types);
			}
			
			if(isMember) {
				//show the list of groups
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
				List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				List<String> groupLinkNames = new ArrayList<>(groups.size());
				for(BusinessGroup group:groups) {
					String groupLinkName = "grp_" + ++cmpcount;
					FormLink link = uifactory.addFormLink(groupLinkName, "group", group.getName(), null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					link.setUserObject(group.getKey());
					groupLinkNames.add(groupLinkName);
				}
				layoutCont.contextPut("groups", groupLinkNames);
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(commentsCtrl == source) {
			
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEdit(ureq);
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(cmc);
		commentsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("category".equals(cmd)) {
				Long categoryKey = (Long)link.getUserObject();
				doOpenCategory(ureq, categoryKey);
			} else if("mark".equals(cmd)) {
				boolean marked = doMark();
				markLink.setIconCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			} else if("comments".equals(cmd)) {
				doOpenComments(ureq);
			} else if("start".equals(cmd)) {
				doStart(ureq);
			} else if("group".equals(cmd)) {
				Long groupKey = (Long)link.getUserObject();
				doOpenGroup(ureq, groupKey);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	public void doEdit(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);

		editCtrl = new AuthoringEditEntryController(ureq, getWindowControl(), stackPanel, row);
		listenTo(editCtrl);
	}
	
	private void doStart(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenCategory(UserRequest ureq, Long categoryKey) {
		String businessPath = "[CatalogEntry:" + categoryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private boolean doMark() {
		OLATResourceable item = OresHelper.clone(entry);
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}
	
	private void doOpenComments(UserRequest ureq) {
		if(commentsCtrl != null) return;
		
		boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), row.getRepositoryEntryResourceable(), null, secCallback);
		listenTo(commentsCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", commentsCtrl.getInitialComponent(), true, translate("comments"));
		listenTo(cmc);
		cmc.activate();
	}
}
