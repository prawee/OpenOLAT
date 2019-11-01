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
package org.olat.course.nodeaccess.manager;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.logging.Tracing;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.navigation.NodeVisitedListener;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class NodeAccessServiceImpl implements NodeAccessService, NodeVisitedListener {

	private static final Logger log = Tracing.createLoggerFor(NodeAccessServiceImpl.class);

	@Autowired
	private List<NodeAccessProvider> nodeAccessProviders;
	
	private NodeAccessProvider getNodeAccessProvider(NodeAccessType type) {
		for (NodeAccessProvider provider : nodeAccessProviders) {
			if (provider.getType().equals(type.getType())) {
				return provider;
			}
		}
		log.error("No node access provider found for type '{}'!", type.getType());
		return null;
	}
	
	@Override
	public List<? extends NodeAccessProviderIdentifier> getNodeAccessProviderIdentifer() {
		return nodeAccessProviders;
	}

	@Override
	public String getNodeAccessTypeName(NodeAccessType type, Locale locale) {
		return getNodeAccessProvider(type).getDisplayName(locale);
	}

	@Override
	public boolean isSupported(NodeAccessType type, String courseNodeType) {
		return getNodeAccessProvider(type).isSupported(courseNodeType);
	}

	@Override
	public boolean isSupported(NodeAccessType type, CourseNode courseNode) {
		return isSupported(type, courseNode.getType());
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, NodeAccessType type,
			CourseNode courseNode, UserCourseEnvironment userCourseEnv, CourseEditorTreeModel editorModel) {
		return getNodeAccessProvider(type).createEditController(ureq, wControl, courseNode, userCourseEnv, editorModel);
	}

	@Override
	public String getCourseTreeCssClass(NodeAccessType type) {
		return getNodeAccessProvider(type).getCourseTreeCssClass();
	}
	
	@Override
	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		return getNodeAccessProvider(type).getCourseTreeModelBuilder(userCourseEnv);
	}

	@Override
	public boolean isAssessmentConfirmationEnabled(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		return getNodeAccessProvider(type).isAssessmentConfirmationEnabled(courseNode, userCourseEnv);
	}

	@Override
	public void onAssessmentConfirmed(CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean confirmed) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		getNodeAccessProvider(type).onAssessmentConfirmed(courseNode, userCourseEnv, confirmed);
	}

	@Override
	public void onScoreUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Float score,
			Boolean userVisibility, Role by) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		getNodeAccessProvider(type).onScoreUpdated(courseNode, userCourseEnv, score, userVisibility, by);
	}

	@Override
	public void onPassedUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Boolean passed,
			Boolean userVisibility, Role by) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		getNodeAccessProvider(type).onPassedUpdated(courseNode, userCourseEnv, passed, userVisibility, by);
	}

	@Override
	public void onStatusUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			AssessmentEntryStatus status, Role by) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		getNodeAccessProvider(type).onStatusUpdated(courseNode, userCourseEnv, status, by);
	}

	@Override
	public boolean onNodeVisited(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		NodeAccessType type = NodeAccessType.of(userCourseEnv);
		return getNodeAccessProvider(type).onNodeVisited(courseNode, userCourseEnv);
	}
}