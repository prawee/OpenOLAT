/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Felix Jost
 */
public class NodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_VISIBILITY = "pane.tab.visibility";
	private static final String PANE_TAB_GENERAL = "pane.tab.general";
	
  /** Configuration key: use spash-scree start page when accessing a course node. Values: true, false **/
  public static final String CONFIG_STARTPAGE = "startpage";
  /** Configuration key: integrate component menu into course menu. Values: true, false **/
  public static final String CONFIG_COMPONENT_MENU = "menuon";
  /** Configuration key: how to integrate the course node content into the course **/
  public static final String CONFIG_INTEGRATION = "integration";
  /** To enforce the encoding of the content of the node **/
	public final static String CONFIG_CONTENT_ENCODING = "encodingContent";
  /** Try to discovery automatically the encoding of the node content **/
	public final static String CONFIG_CONTENT_ENCODING_AUTO = "auto";
  /** To enforce the encoding of the embedded javascript of the node **/
	public final static String CONFIG_JS_ENCODING = "encodingJS";
  /** Take the same encoding as the content **/
	public final static String CONFIG_JS_ENCODING_AUTO = "auto";
	
	private CourseNode courseNode;
	private VelocityContainer descriptionVc;

	private NodeConfigFormController nodeConfigController;

	private TabbedPane myTabbedPane;
	private VisibilityEditController visibilityEditCtrl;
	private TabbableController nodeAccessCtrl;
	private TabbableController childTabsCntrllr;

	/** Event that signals that the node configuration has been changed * */
	public static final Event NODECONFIG_CHANGED_EVENT = new Event("nodeconfigchanged");
	private static final String[] paneKeys = { PANE_TAB_VISIBILITY, PANE_TAB_GENERAL };
	
	@Autowired
	private NodeAccessService nodeAccessService;

	public NodeEditController(UserRequest ureq, WindowControl wControl, ICourse course,
			CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, TabbableController childTabsController) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		
		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		this.childTabsCntrllr = childTabsController;
		listenTo(childTabsCntrllr);
		
		// description and metadata component
		descriptionVc = createVelocityContainer("nodeedit");
		descriptionVc.setDomReplacementWrapperRequired(false);
		Long repoKey = RepositoryManager.getInstance().lookupRepositoryEntryKey(course, true);
		
		StringBuilder extLink = new StringBuilder();
		extLink.append(Settings.getServerContextPathURI())
			.append("/url/RepositoryEntry/").append(repoKey)
			.append("/CourseNode/").append(courseNode.getIdent());
		StringBuilder intLink = new StringBuilder();
		intLink.append("javascript:parent.gotonode(").append(courseNode.getIdent()).append(")");
		
		descriptionVc.contextPut("extLink", extLink.toString());
		descriptionVc.contextPut("intLink", intLink.toString());
		descriptionVc.contextPut("nodeId", courseNode.getIdent());
		
		putInitialPanel(descriptionVc);

		nodeConfigController = new NodeConfigFormController(ureq, wControl, courseNode, repoKey);
		listenTo(nodeConfigController);
		descriptionVc.put("nodeConfigForm", nodeConfigController.getInitialComponent());
		
		NodeAccessType nodeAccessType = course.getCourseConfig().getNodeAccessType();
		if (nodeAccessService.isSupported(nodeAccessType, courseNode)) {
			TabbableController nodeAccessCtrl = nodeAccessService.createEditController(ureq, getWindowControl(),
					nodeAccessType, courseNode, userCourseEnvironment, course.getEditorTreeModel());
			if (nodeAccessCtrl != null) {
				this.nodeAccessCtrl = nodeAccessCtrl;
				listenTo(nodeAccessCtrl);
			} else if (ConditionNodeAccessProvider.TYPE.equals(nodeAccessType.getType())) {
				// fallback for legacy access edit controller
				visibilityEditCtrl = new VisibilityEditController(ureq, getWindowControl(), courseNode,
						userCourseEnvironment, course.getEditorTreeModel());
				listenTo(visibilityEditCtrl);
			}	
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// Don't do anything.
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == visibilityEditCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == nodeAccessCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == childTabsCntrllr) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == nodeConfigController) {
			if (event == Event.DONE_EVENT) {
				courseNode.setShortTitle(nodeConfigController.getMenuTitle());
				courseNode.setLongTitle(nodeConfigController.getDisplayTitle());
				courseNode.setLearningObjectives(nodeConfigController.getLearningObjectives());
				courseNode.setDisplayOption(nodeConfigController.getDisplayOption());
			}
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_EDITED, getClass());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_GENERAL), descriptionVc);
		if (nodeAccessCtrl != null) {
			nodeAccessCtrl.addTabs(tabbedPane);
		}
		if (visibilityEditCtrl !=null) {
			tabbedPane.addTab(translate(PANE_TAB_VISIBILITY), visibilityEditCtrl.getInitialComponent());
		}
		if (childTabsCntrllr != null) {
			childTabsCntrllr.addTabs(tabbedPane);
		}
	}

	@Override
	protected ActivateableTabbableDefaultController[] getChildren() {
		if (childTabsCntrllr != null && childTabsCntrllr instanceof ActivateableTabbableDefaultController) {
			return new ActivateableTabbableDefaultController[] { (ActivateableTabbableDefaultController) childTabsCntrllr };
		}
		return new ActivateableTabbableDefaultController[] {};
	}

}
