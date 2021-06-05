package org.acme.getting.started;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/hello")
public class GreetingResource {

    @ConfigProperty(name = "birt.report")
    String reportPath;

    private IReportEngine birtEngine;
    private Map<String, IReportRunnable> reports = new HashMap<>();

    @PostConstruct
    public void before() throws BirtException, FileNotFoundException {
        EngineConfig config = new EngineConfig();
        try {
            Platform.startup(config);
        } catch (BirtException e) {
            e.printStackTrace();
        }
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        birtEngine = factory.createReportEngine(config);
        loadReports();
    }

    public void loadReports() throws EngineException, FileNotFoundException {
        File file = new File(this.getClass().getClassLoader()
            .getResource(reportPath + File.separatorChar + "report-1.rptdesign").getFile());
        System.out.println(new File(this.getClass().getClassLoader()
            .getResource(reportPath + File.separatorChar + "customerData.csv").getPath()));

        reports.put("report-1", birtEngine.openReportDesign(new FileInputStream(file)));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws EngineException {
        generatePDFReport(this.reports.get("report-1"));
        return "Hello RESTEasy";
    }

    private void generatePDFReport(IReportRunnable report) throws EngineException {
        IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(report);
        PDFRenderOption pdfRenderOption = new PDFRenderOption();
        pdfRenderOption.setOutputFormat("pdf");
        pdfRenderOption.setOutputFileName("cust.pdf");
        runAndRenderTask.setRenderOption(pdfRenderOption);
        runAndRenderTask.run();
        runAndRenderTask.close();
    }
}