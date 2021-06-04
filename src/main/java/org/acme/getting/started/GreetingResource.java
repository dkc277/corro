package org.acme.getting.started;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

@Path("/hello")
public class GreetingResource {

    private IReportEngine birtEngine;
    private Map<String, IReportRunnable> reports = new HashMap<>();

    @PostConstruct
    public void before() throws BirtException {
        EngineConfig config = new EngineConfig();
        try {
            Platform.startup(config);
        } catch (BirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        birtEngine = factory.createReportEngine(config);
        loadReports();
    }

    public void loadReports() throws EngineException {
        File file = new File("new_report.rptdesign");
        reports.put("new_report", birtEngine.openReportDesign(file.getAbsolutePath()));

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        generatePDFReport(this.reports.get("new_report"));
        return "Hello RESTEasy";
    }

    private void generatePDFReport(IReportRunnable report) {
        IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(report);
        // response.setContentType(birtEngine.getMIMEType("pdf"));
        IRenderOption options = new RenderOption();
        PDFRenderOption pdfRenderOption = new PDFRenderOption(options);
        pdfRenderOption.setOutputFormat("pdf");
        runAndRenderTask.setRenderOption(pdfRenderOption);
        runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_PDF_RENDER_CONTEXT, this);
        // FileOutputStream fout = null;
        try (FileOutputStream fout = new FileOutputStream(new File("out.pdf"))){
            // fout = new FileOutputStream(new File("out.pdf"));
            pdfRenderOption.setOutputStream(fout);
            runAndRenderTask.run();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            runAndRenderTask.close();
            // if (fout != null) {
            //     try {
            //         fout.close();
            //     } catch (IOException ex) {
            //         // TODO Auto-generated catch block
            //         ex.printStackTrace();
            //     }
            // }
        }
    }

    // public List<Report> getReports() {
    // List<Report> response = new ArrayList<>();
    // for (Map.Entry<String, IReportRunnable> entry : reports.entrySet()) {
    // IReportRunnable report = reports.get(entry.getKey());
    // IGetParameterDefinitionTask task =
    // birtEngine.createGetParameterDefinitionTask(report);
    // Report reportItem = new
    // Report(report.getDesignHandle().getProperty("title").toString(),
    // entry.getKey());
    // for (Object h : task.getParameterDefns(false)) {
    // IParameterDefn def = (IParameterDefn) h;
    // reportItem.getParameters()
    // .add(new Report.Parameter(def.getPromptText(), def.getName(),
    // getParameterType(def)));
    // }
    // response.add(reportItem);
    // }
    // return response;
    // }
}