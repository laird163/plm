package ext.plm.component;

import java.io.PrintWriter;
import java.io.Writer;

import wt.util.HTMLEncoder;

import com.ptc.core.components.rendering.AbstractRenderer;
import com.ptc.core.components.rendering.Renderer;
import com.ptc.core.components.rendering.RenderingContext;
import com.ptc.core.components.rendering.RenderingException;
import com.ptc.core.components.rendering.renderers.HTMLGuiComponentRenderer;

public  class FileComponentRenderer extends AbstractRenderer{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected boolean isValidForObject(Object o) {
		// TODO Auto-generated method stub
		return o instanceof FileComponent;
	}

	protected void renderObject(FileComponent fileComponent, PrintWriter printWriter, RenderingContext renderingContext)
			throws RenderingException {
		StringBuffer sb = new StringBuffer();
		sb.append("<input type=\"" + fileComponent.getInputType() + "\"" + HTMLGuiComponentRenderer.getIdString(fileComponent.getId()) + " " + fileComponent.getStyleClasses() + " name=\"" + HTMLEncoder.encodeForHTMLAttribute(this.getComponentName(fileComponent, renderingContext)) + "\"" + HTMLGuiComponentRenderer.getDisabledString(fileComponent.isEditable()) + HTMLGuiComponentRenderer.getTooltipString(fileComponent.getTooltip()) + HTMLGuiComponentRenderer.getJsActionsString(fileComponent.getJsActions()) + ">" + fileComponent.getLabel() + " </>");
		sb.append("<input type=\"button\""+" id=\"upJS\""+" onclick=\"UpladFile()\"" +"style=\"width: 85px; height: 28px;\""+ "value=\"上传附件\">");
		AbstractRenderer.write(printWriter, sb.toString());		
	}

	@Override
	protected void renderObject(Object o, PrintWriter printWriter,
			RenderingContext renderingContext) throws RenderingException {
		this.renderObject((FileComponent)o, printWriter, renderingContext);
		
	}

}
