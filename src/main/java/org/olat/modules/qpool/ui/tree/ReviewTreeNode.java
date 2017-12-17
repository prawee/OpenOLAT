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
package org.olat.modules.qpool.ui.tree;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.datasource.ReviewItemsSource;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 17.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewTreeNode extends GenericTreeNode implements ControllerTreeNode {

	private static final long serialVersionUID = 5117905919137046164L;

	private static final String REVIEW = "review";

	private final TooledStackedPanel stackPanel;
	private QuestionsController questionsCtrl;
	
	private final QPoolSecurityCallback securityCallback;
	private final TaxonomyLevel taxonomyLevel;

	public ReviewTreeNode(TooledStackedPanel stackPanel, QPoolSecurityCallback securityCallback,
			TaxonomyLevel taxonomyLevel) {
		super();
		this.stackPanel = stackPanel;
		this.securityCallback = securityCallback;
		this.taxonomyLevel = taxonomyLevel;
		
		this.setTitle(taxonomyLevel.getDisplayName());
		
		this.setUserObject(taxonomyLevel);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		QuestionItemsSource source = new ReviewItemsSource(
				ureq.getIdentity(),
				ureq.getUserSession().getRoles(),
				taxonomyLevel);
		if (questionsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(REVIEW + "_" + taxonomyLevel.getIdentifier(), taxonomyLevel.getKey());
			WindowControl swControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, null, wControl, true);
			questionsCtrl = new QuestionsController(ureq, swControl, stackPanel, source, securityCallback,
					REVIEW + taxonomyLevel.getKey());
		} else {
			questionsCtrl.updateSource(source);
		}
		return questionsCtrl;
	}

}