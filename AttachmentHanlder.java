package ext.plm.component;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ptc.netmarkets.bookmark.StandardNmBookmarkService;
import com.ptc.netmarkets.model.NmOid;

import wt.content.ApplicationData;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.fv.master.RedirectDownload;
import wt.fv.uploadtocache.CachedContentDescriptor;
import wt.fv.uploadtocache.UploadToCacheHelper;
import wt.inf.container.WTContainerRef;
import wt.pom.Transaction;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.EncodingConverter;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.vc.VersionReference;
import wt.workflow.collaboration.CollaborationContainer;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfProcess;
import wt.workflow.notebook.ImportedBookmark;
import wt.workflow.notebook.Notebook;
import wt.workflow.notebook.NotebookHelper;
import wt.workflow.notebook.NotebookUtil;
import wt.workflow.notebook.SubjectOfNotebook;
import wt.workflow.work.WorkItem;
import ext.plm.util.CommonUtil;

@Controller
public class AttachmentHanlder {
	@RequestMapping(value = "/ext/plm/component/planActivity", method = {
			RequestMethod.POST, RequestMethod.GET })
	public void planActivity(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setHeader("Content-Type", "text/html");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Expires", "0");
		String temp = request.getServletContext().getRealPath("/") + "temp"; // 临时目录
		//String loadpath = request.getServletContext().getRealPath("/") + "dir";
		String pageoid = request.getParameter("pageoid");
		String action = request.getParameter("action");
		String ibookid =request.getParameter("ibookid");
		JSONObject result = new JSONObject();
		if(("clear").equals(action)){
			
			String message =deleteAttachements(ibookid);
			
			if(message!=null){
				result.put("success", "false");
				result.put("result", message);
				response.getWriter().println(result.toJSONString());
				return;
			}
			result.put("result", "清除成功");
			result.put("success", "true");
			result.put("action", "clear");
			response.getWriter().println(result.toJSONString());
			return;
		}
		File tempFile = new File(temp);
		if (!tempFile.exists()) {
			tempFile.mkdirs();
		}

		/*File loadpathFile = new File(loadpath);
		if (!loadpathFile.exists()) {
			loadpathFile.mkdirs();
		}*/
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(new File(temp));
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");
		List fileItems = upload.parseRequest(request);
		Iterator iter = fileItems.iterator();
		String returnResult = "";
		Transaction transaction = new Transaction();
		URL viewContentURL;
		transaction.start();
		String itemName = "";
		try {
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();

				if (!item.isFormField()) {
					itemName = item.getName();
					if (itemName.contains("\\")) {
						itemName = itemName.substring(
								itemName.lastIndexOf("\\") + 1,
								itemName.length());
					}
					CollaborationContainer collaborationContainer = createCollaborationContainer(
							new NmOid(pageoid), itemName);
					ImportedBookmark importedBookmark = ImportedBookmark
							.newImportedBookmark(itemName,
									collaborationContainer);
					
					importedBookmark = (ImportedBookmark) PersistenceHelper.manager
							.save(importedBookmark);
					if (importedBookmark != null) {
						ContentHolder contentHolder = (ContentHolder) importedBookmark;

						ApplicationData applicationData = ApplicationData
								.newApplicationData(contentHolder);
						applicationData.setFileName(itemName);
						applicationData.setRole(ContentRoleType.PRIMARY);
						ContentServerHelper.service.updateContent(
								contentHolder, applicationData,
								item.getInputStream());
						
						final WTReference subject = NotebookUtil.getSubject(
								collaborationContainer, false);
						if (subject instanceof VersionReference) {
							importedBookmark
									.setSubjectVersionReference((VersionReference) subject);
						} else {
							importedBookmark
									.setSubjectObjectReference((ObjectReference) subject);
						}
						PersistenceHelper.manager.save(importedBookmark);
						String s3 = RedirectDownload.getPreferredURL((ApplicationData)applicationData, (ContentHolder)importedBookmark).toString();
						result.put("url",s3);
						result.put("itemName", itemName);
						result.put("result", "上传成功");
						result.put("success", "true");
						result.put("ibookid", importedBookmark.getIdentity());
						
					}

					transaction.commit();
					transaction = null;
				} else {
					result.put("result", "未上传附件，请先选择文件后再上传");
					result.put("success", "false");

				}
			}

		} catch (Exception e) {
			returnResult = e.toString();
		} finally {
			if (transaction != null) {
				transaction.rollback();
			}
			 /*File TrxFiles[] = tempFile.listFiles();
		        for(File curFile:TrxFiles ){
		            curFile.delete();  
		        }*/
		}
		if (returnResult != null && returnResult.trim().length() > 0) {
			result.put("success", "false");
			result.put("result", returnResult);
		}
		result.put("action", "upload");
		response.getWriter().println(result.toJSONString());
	}

	public CollaborationContainer createCollaborationContainer(NmOid nnoid,
			String fileName) throws ObjectNoLongerExistsException, WTException {
		// EncodingConverter.unicodeToAscii(contentIdentity);
		Notebook notebook = null;
		if (nnoid != null) {
			WTObject parentProcess = (WTObject) PersistenceHelper.manager
					.refresh(nnoid.getOid());
			if (parentProcess instanceof WorkItem) {
				final Persistable object = ((WfActivity) ((WorkItem) parentProcess)
						.getSource().getObject()).getParentProcessRef()
						.getObject();
				if (object instanceof WfBlock) {
					parentProcess = ((WfBlock) object).getParentProcess();
				} else {
					parentProcess = (WfProcess) object;
				}
			}
			final Enumeration notebooks = NotebookHelper.service
					.getNotebooks(parentProcess);
			if (notebooks != null && notebooks.hasMoreElements()) {
				notebook = (Notebook) notebooks.nextElement();
			} else {
				final SessionContext context = SessionContext.newContext();
				try {
					SessionHelper.manager.setAdministrator();
					final WTContainerRef containerRef = nnoid.getContainerRef();
					WTObject wtObject;
					if (parentProcess instanceof WorkItem) {
						wtObject = (WTObject) ((WorkItem) parentProcess)
								.getPrimaryBusinessObject().getObject();
					} else {
						wtObject = parentProcess;
					}
					notebook = NotebookHelper.service
							.createNotebook(
									NotebookHelper.service
											.getDefaultTemplate(containerRef),
									WTMessage
											.getLocalizedMessage(
													"com.ptc.netmarkets.notebookfolder.notebookfolderResource",
													"0", null,
													SessionHelper.getLocale()),
									(SubjectOfNotebook) wtObject, containerRef);
				} finally {
					SessionContext.setContext(context);
				}
			}

		}
		return notebook;
	}
	public String deleteAttachements(String nnoid){
		    String ret=null;
		    ReferenceFactory rf = new ReferenceFactory();
			try {
				WTReference wrf =rf.getReference(nnoid);
				if(wrf==null) return ret;
				 Persistable p= wrf.getObject();
				PersistenceHelper.manager.delete(p);
				/*QueryResult qr =queryIBook(nnoid);
				while(qr.hasMoreElements()){
					PersistenceHelper.manager.delete((ImportedBookmark)qr.nextElement());
				}*/
			} catch (QueryException e) {
				ret=e.getLocalizedMessage();
				e.printStackTrace();
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ret=e.getLocalizedMessage();
			}
			return ret;

	}
	public QueryResult queryIBook(NmOid nmoid) throws WTException{
		QuerySpec qs  = new QuerySpec(ImportedBookmark.class);
		qs.appendWhere(new SearchCondition(ImportedBookmark.class,"subjectObjectReference.key.id","=",nmoid.getOid().getId()));
		QueryResult qr =PersistenceHelper.manager.find(qs);
		return qr;
	}
}
