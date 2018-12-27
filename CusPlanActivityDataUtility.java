package ext.plm.project.datautility;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.fv.master.RedirectDownload;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueUtility;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.workflow.notebook.ImportedBookmark;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.ComboBox;
import com.ptc.core.components.rendering.guicomponents.GUIComponentArray;
import com.ptc.core.components.rendering.guicomponents.HTMLGuiComponent;
import com.ptc.core.components.rendering.guicomponents.PushButton;
import com.ptc.core.components.rendering.guicomponents.PushButton.ButtonType;
import com.ptc.core.components.rendering.guicomponents.TextArea;
import com.ptc.core.components.rendering.guicomponents.TextBox;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;
import com.ptc.core.components.rendering.guicomponents.UrlDisplayComponent;
import com.ptc.core.components.rendering.guicomponents.UrlInputComponent;
import com.ptc.core.meta.type.common.impl.DefaultTypeInstance;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.projectmanagement.deliverable.PlanDeliverable;
import com.ptc.projectmanagement.deliverable.datautilities.PlanDeliverableDataUtility;
import com.ptc.projectmanagement.plan.Plan;
import com.ptc.projectmanagement.plan.PlanActivity;
import com.ptc.projectmanagement.plan.datautilities.PlanActivityDataUtility;

import ext.customer.common.MBAUtil;
import ext.plm.component.FileComponent;
import ext.plm.project.util.ProjectHelper;

public class CusPlanActivityDataUtility extends PlanActivityDataUtility {

	public Object getDataValue(final String s, final Object o,
			final ModelContext modelContext) throws WTException {

		NmCommandBean nmCommandBean =modelContext.getNmCommandBean();
		boolean flag =ext.plm.project.util.ProjectHelper.checkccmProj(nmCommandBean);
		if (flag) {
			final ReferenceFactory referenceFactory = new ReferenceFactory();
			HashMap<String, String> attrs = new HashMap<String, String>();
			if (o instanceof DefaultTypeInstance) {
				DefaultTypeInstance dti = (DefaultTypeInstance) o;
				String ide = dti.getPersistenceIdentifier();
				if (ide != null) { //编辑活动
					final WTReference reference = referenceFactory
							.getReference(dti.getPersistenceIdentifier());
					Object object = reference.getObject();
					if (object instanceof PlanActivity) {
						PlanActivity pa =(PlanActivity) object;
						  String value = (String) MBAUtil.getObjectValue(pa, s);
						  attrs.put(s, value);
					
						if(pa.getParent() instanceof Plan){//阶段任务
							if(s.equals("IsSkipCurrent")){
								ComboBox cb = new ComboBox();
								ArrayList<String> internalValues = new ArrayList<String>(
										Arrays.asList("", "是", "否"));
								ArrayList<String> displaylValues = new ArrayList<String>(
										Arrays.asList("", "是", "否"));
								cb.setInternalValues(internalValues);
								cb.setValues(displaylValues);
								cb.setEnabled(true);
								cb.setSelected(attrs.get(s));
								cb.setColumnName(AttributeDataUtilityHelper.getColumnName(s, o,
										modelContext));
								return cb;
							}else if(s.equals("remarkReason")){
								TextArea area= new TextArea();
								area.setName("remarkReason");
								area.setHeight(4);
								area.setWidth(57);
								area.setValue(attrs.get(s));
								return area;
							}else if (s.equals("uplodadRemarkFile")){
								return displayAttached(pa, nmCommandBean);
							}
					
						}else{//阶段子活动
							if(s.equals("IsSkipCurrent")){
								ComboBox cb = new ComboBox();
								ArrayList<String> internalValues = new ArrayList<String>(
										Arrays.asList("", "是", "否"));
								ArrayList<String> displaylValues = new ArrayList<String>(
										Arrays.asList("", "是", "否"));
								cb.setInternalValues(internalValues);
								cb.setValues(displaylValues);
								cb.setEditable(false);
								cb.setSelected(attrs.get(s));
								cb.setColumnName(AttributeDataUtilityHelper.getColumnName(s, o,
										modelContext));
								return cb;
							}else if(s.equals("remarkReason")){
								TextArea area= new TextArea();
								area.setName("remarkReason");
								area.setHeight(4);
								area.setWidth(57);
								area.setEditable(false);
								return area;
							}
							else if (s.equals("uplodadRemarkFile")){
								return displayAttached(pa, nmCommandBean);
							}
						}
						}
				}else{//创建
				//	isCreateChildActivityParam=true
					Object obj =nmCommandBean.getParameterMap().get("isCreateChildActivityParam");	
					System.out.println(obj);

					if(s.equals("IsSkipCurrent")){
						ComboBox cb = new ComboBox();
						ArrayList<String> internalValues = new ArrayList<String>(
								Arrays.asList("", "是", "否"));
						ArrayList<String> displaylValues = new ArrayList<String>(
								Arrays.asList("", "是", "否"));
						cb.setInternalValues(internalValues);
						cb.setValues(displaylValues);
						if(obj!=null){
							cb.setEditable(false);
						}
						cb.setSelected(attrs.get(s));
						cb.setColumnName(AttributeDataUtilityHelper.getColumnName(s, o,
								modelContext));
						return cb;
					}else if(s.equals("remarkReason")){
						TextArea area= new TextArea();
						area.setName("remarkReason");
						area.setHeight(4);
						area.setWidth(57);
						if(obj!=null){
							area.setEditable(false);
							
						}
						
						return area;
					}else if (s.equals("uplodadRemarkFile")){
						return displayAttached(null, nmCommandBean);

					}
					
				}
			}
			else if (o instanceof PlanActivity){ //查看
				PlanActivity pa =(PlanActivity) o;
				if("uplodadRemarkFile".equals(s)){
					
						HashMap map =getAttachement(pa);
						final StringBuffer sb = new StringBuffer();
						sb.append("<A HREF=");
						sb.append(map.get("url")==null?"":map.get("url"));
						sb.append(">");
						sb.append(map.get("ap")==null?"":map.get("ap"));
						sb.append("</A>");
						final TextDisplayComponent textDisplayComponent = new TextDisplayComponent(sb.toString());
						textDisplayComponent.setCheckXSS(false);
						textDisplayComponent.setValue(sb.toString());
						textDisplayComponent.setId("linkid");
					    return textDisplayComponent;
					
				}
				String value = (String) MBAUtil.getObjectValue(pa, s);
				return value;
			}
		

		}else{
			if(s.equals("IsSkipCurrent")){
				ComboBox cb = new ComboBox();
				ArrayList<String> internalValues = new ArrayList<String>(
						Arrays.asList("", "是", "否"));
				ArrayList<String> displaylValues = new ArrayList<String>(
						Arrays.asList("", "是", "否"));
				cb.setInternalValues(internalValues);
				cb.setValues(displaylValues);
					cb.setEditable(false);
				
				cb.setColumnName(AttributeDataUtilityHelper.getColumnName(s, o,
						modelContext));
				return cb;
			}else if(s.equals("remarkReason")){
				TextArea area= new TextArea();
				area.setName("remarkReason");
				area.setHeight(4);
				area.setWidth(57);
				area.setEditable(false);			
				return area;
			}
		}
       return null;
	}
	public HashMap getAttachement(Persistable o) throws WTException{
		QueryResult qr =queryIBook( o);
		HashMap map =new HashMap();
		if(qr==null||! (o instanceof PlanActivity)){
			return map;
		}
		String s=null;
		ApplicationData applicationData=null;
		while(qr.hasMoreElements()){
			ImportedBookmark ib =(ImportedBookmark) qr.nextElement();
			
			QueryResult qr1 = ContentHelper.service.getContentsByRole(ib,
					ContentRoleType.PRIMARY);
			while(qr1.hasMoreElements()){
				 applicationData=	(ApplicationData) qr1.nextElement();
				 map.put("ap", applicationData.getFileName());
				try {
					 s= RedirectDownload.getPreferredURL((ApplicationData)applicationData, (ContentHolder)ib).toString();
					 map.put("url", s);
				} catch (IOException e) {
					e.printStackTrace();
					throw new WTException("获取附件失败");
				}
				map.put("ibook", ib.getIdentity());
				
				break;
			}
		}
		return map;
	}
	public QueryResult queryIBook(Persistable p) throws WTException{
		if (p==null){
			return null;
		}
		QuerySpec qs  = new QuerySpec(ImportedBookmark.class);
		qs.appendWhere(new SearchCondition(ImportedBookmark.class,"subjectObjectReference.key.id","=",p.getPersistInfo().getObjectIdentifier().getId()));
		QueryResult qr =PersistenceHelper.manager.find(qs);
		return qr;
	}
	
	public GUIComponentArray displayAttached(Persistable p , NmCommandBean nmCommandBean) throws WTException{

		HashMap map =getAttachement(p);

		GUIComponentArray array = new GUIComponentArray();
		
		final StringBuffer sb = new StringBuffer();
		sb.append("<A HREF=");
		sb.append(map.get("url")==null?"":map.get("url"));
		sb.append(">");
		sb.append(map.get("ap")==null?"":map.get("ap"));
		sb.append("</A>");
		final TextDisplayComponent textDisplayComponent = new TextDisplayComponent(sb.toString());
		textDisplayComponent.setCheckXSS(false);
		textDisplayComponent.setValue(sb.toString());
		textDisplayComponent.setId("linkid");
		array.addGUIComponent(textDisplayComponent);
		
		FileComponent fc = new FileComponent();
		fc.setLabel("");
		fc.setName("upload");
		array.addGUIComponent(fc);
		
		PushButton pb =new PushButton("清除");
		pb.setName("clear");
		pb.setButtonType(ButtonType.RESET);
		pb.addJsAction("onClick", "clearFile()");
		array.addGUIComponent(pb);
		
		TextBox text = new TextBox();
		text.setName("pageoid");
		text.setId("pageoid");
		text.setValue(nmCommandBean.getPrimaryOid()==null?"":nmCommandBean.getPrimaryOid().toString());
		text.setHidden(true);
		text.setInputType("hidden");
		array.addGUIComponent(text);
		
		TextBox iptext = new TextBox();
		iptext.setName("ibookid");
		iptext.setId("ibookid");
		iptext.setValue(map.get("ibook")==null?"":map.get("ibook").toString());
		iptext.setHidden(true);
		iptext.setInputType("hidden");
		array.addGUIComponent(iptext);
		
		
		return array;
	
	}
}
