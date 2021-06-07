package org.acme.getting.started;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
public class GreetingResource {

    private static final String REPORT_1 = "report-1";

    @ConfigProperty(name = "birt.report")
    String reportPath;

    private IReportEngine birtEngine;
    private Map<String, IReportRunnable> reports = new HashMap<>();

    @PostConstruct
    public void before() throws BirtException, FileNotFoundException {
        EngineConfig config = new EngineConfig();
        Platform.startup(config);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        birtEngine = factory.createReportEngine(config);

        System.out.println("factory:" + factory);
        birtEngine = factory.createReportEngine(config);
        loadReports();
    }

    public void loadReports() throws EngineException, FileNotFoundException {
        File file = new File("target/classes/report-1.rptdesign");

        System.out.println(file.getPath());

        reports.put(REPORT_1, birtEngine.openReportDesign(new FileInputStream(file)));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws EngineException {
        generatePDFReport(this.reports.get(REPORT_1));
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