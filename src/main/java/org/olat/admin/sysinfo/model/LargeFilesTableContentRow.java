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
package org.olat.admin.sysinfo.model;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class LargeFilesTableContentRow {

	private final Long key;
	private final String name;
	private final Long size;
	private FormLink pathInfo;
	private final String path;
	private final Identity author;
	private final boolean revision;
	private final String fileType;
	private final String fileCategory;
	private final LicenseType license;
	private final Date createdAt;
	private final Date lastModifiedAt;
	private final boolean trashed;
	private final String uuid;
	private final int downloadCount;
	private final String title;
	private final String comment;
	private final String publisher;
	private final String source;
	private final String creator;
	private final Date pubDate;
	private final String language;
	private final boolean locked;
	private final Date lockedAt;
	private final Identity lockedBy;
	private final long revisionNr;
	private final String revisionComment;

	public LargeFilesTableContentRow(VFSMetadata metadata) {
		key = metadata.getKey();
		name = metadata.getFilename();
		size = metadata.getFileSize();
		author = metadata.getAuthor();
		path = metadata.getRelativePath();
		fileType = WebappHelper.getMimeType(metadata.getFilename()) != null ? WebappHelper.getMimeType(metadata.getFilename()).split("/")[1] : "Unknown";
		fileCategory = WebappHelper.getMimeType(metadata.getFilename()) != null ? WebappHelper.getMimeType(metadata.getFilename()).split("/")[0] : "Unknown";
		revision = false;
		createdAt = metadata.getCreationDate();
		lastModifiedAt = metadata.getLastModified();
		license = metadata.getLicenseType();
		trashed = metadata.isDeleted();
		uuid = metadata.getUuid();
		downloadCount = metadata.getDownloadCount();
		title = metadata.getTitle();
		comment = metadata.getComment();
		publisher = metadata.getPublisher();
		source = metadata.getSource();
		creator = metadata.getCreator();
		pubDate = calculateDateFromPublicationDateArray(metadata.getPublicationDate());
		language = metadata.getLanguage();
		locked = metadata.isLocked();
		lockedAt = metadata.getLockedDate();
		lockedBy = metadata.getLockedBy();
		revisionNr = metadata.getRevisionNr();
		revisionComment = metadata.getRevisionComment();
	}

	public LargeFilesTableContentRow(VFSRevision rev) {
		key = rev.getMetadata().getKey();
		name = rev.getFilename();
		size = rev.getSize();
		author = rev.getAuthor();
		path = rev.getMetadata().getRelativePath();
		revision = true;
		fileType = WebappHelper.getMimeType(rev.getFilename()) != null ? WebappHelper.getMimeType(rev.getFilename()).split("/")[1] : "Unknown";
		fileCategory = WebappHelper.getMimeType(rev.getFilename()) != null ? WebappHelper.getMimeType(rev.getFilename()).split("/")[0] : "Unknown";
		createdAt = rev.getCreationDate();
		lastModifiedAt = rev.getFileLastModified();
		license = rev.getMetadata().getLicenseType();
		trashed = rev.getMetadata().isDeleted();
		uuid = rev.getMetadata().getUuid();
		downloadCount = rev.getMetadata().getDownloadCount();
		title = rev.getMetadata().getTitle();
		comment = rev.getMetadata().getComment();
		publisher = rev.getMetadata().getPublisher();
		source = rev.getMetadata().getSource();
		creator = rev.getMetadata().getCreator();
		pubDate = calculateDateFromPublicationDateArray(rev.getMetadata().getPublicationDate());
		language = rev.getMetadata().getLanguage();
		locked = rev.getMetadata().isLocked();
		lockedAt = rev.getMetadata().getLockedDate();
		lockedBy = rev.getMetadata().getLockedBy();
		revisionNr = rev.getRevisionNr();
		revisionComment = rev.getRevisionComment();
	}

	private Date calculateDateFromPublicationDateArray(String[] pubDateArray) {
		if(pubDateArray == null || pubDateArray.length == 0) return null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.clear();
			if(pubDateArray.length > 0 && pubDateArray[0] != null) {
				cal.set(Calendar.YEAR, Integer.parseInt(pubDateArray[0]));
			}
			if(pubDateArray.length > 1 && pubDateArray[1] != null) {
				cal.set(Calendar.MONTH, Integer.parseInt(pubDateArray[1]));
			}
			if(pubDateArray.length > 2 && pubDateArray[2] != null) {
				cal.set(Calendar.DATE, Integer.parseInt(pubDateArray[2]));
			}
			return cal.getTime();
		} catch (NumberFormatException e) {
			// can happen
			return null;
		}
	}

	public Long getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public Long getSize() {
		return size;
	}

	public FormLink getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(FormLink pathInfo) {
		this.pathInfo = pathInfo;
	}

	public Identity getAuthor() {
		return author;
	}

	public String getPath() {
		return path;
	}

	public Boolean isRevision() {
		return revision;
	}

	public String getFileType() {
		return fileType;
	}

	public LicenseType getLicense() {
		return license;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Date getLastModifiedAt() {
		return lastModifiedAt;
	}

	public String getFileCategory() {
		return fileCategory;
	}

	public String getUuid() {
		return uuid;
	}

	public int getDownloadCount() {
		return downloadCount;                 
	}                                         

	public String getTitle() {                
		return title;                         
	}                                         

	public String getComment() {              
		return comment;                       
	}                                         

	public String getPublisher() {            
		return publisher;                     
	}                                         

	public String getSource() {               
		return source;                        
	}                                         

	public String getCreator() {
		return creator;
	}

	public Date getPubDate() {
		return pubDate;
	}
	public Date getLockedAt() {
		return lockedAt;
	}

	public Identity getLockedBy() {
		return lockedBy;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isLocked() {
		return locked;
	}

	public long getRevisionNr() {
		return revisionNr;
	}

	public String getRevisionComment() {
		return revisionComment;
	}

	public Boolean isTrashed() {
		return trashed;
	}

	public long getAge() {
		return (new Date().getTime()) - this.getCreatedAt().getTime();
	}
}
