package cern.c2mon.web.configviewer.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.web.configviewer.service.HistoryService;
import cern.c2mon.web.configviewer.service.TagService;
import cern.c2mon.web.configviewer.util.FormUtility;


/**
 * A controller for the Online Trend Viewer 
 * */
@Controller
public class TrendViewController {

  /** Base URL for the viewer */
  public static final String TREND_VIEW_URL = "/trendviewer/";
  
  /** URL to define Last Records */
  public static final String LAST_RECORDS_URL = "/records/";

  /** A URL to the viewer with input form */
  public static final String TREND_VIEW_FORM_URL = "/trendviewer/form";
  
  /** Title for the form page */
  public static final String TREND_FORM_TITLE = "Trend Viewer";

  /** Instruction for the form page */
  public static final String INSTRUCTION = "Enter a Tag Id to create a Trend View.";

  /** How many records in history to ask for: 100 looks ok! */
  private static final int RECORDS_TO_ASK_FOR = 100;
  
  @Autowired
  /** HistoryService  */
  private HistoryService historyService;
  
  @Autowired
  /** TagService  */
  private TagService tagService;

  /** TrendViewController logger */
  private static Logger logger = Logger.getLogger(TrendViewController.class);

  @RequestMapping(value = TREND_VIEW_URL, method = { RequestMethod.GET })
  public String viewTrend(final Model model) {
    logger.info(TREND_VIEW_URL);
    return ("redirect:" + TREND_VIEW_FORM_URL);
  }    
  
  /**
   * Displays a Trend View for a given id.
   * @param id the last records of the given tag id are being shown
   * @param response the html result is written to that HttpServletResponse response
   * @param lastRecords number of records to be shown
   * @return nothing
   * @throws IOException 
   * */
  @RequestMapping(value = TREND_VIEW_URL + "{id}" 
      + LAST_RECORDS_URL + "{lastRecords}"
        , method = { RequestMethod.GET })
  public String viewTrendLastRecords(@PathVariable(value = "id") final String id,
      @PathVariable(value = "lastRecords") final int lastRecords,
      final Model model) throws IOException  {
    
    logger.info(TREND_VIEW_URL + "{id} " + id + LAST_RECORDS_URL + "{lastRecords} ");

    try {
      final List<HistoryTagValueUpdate> historyValues = 
          historyService.requestHistoryData(id, lastRecords);
      
      final boolean isBooleanData = historyService.isBooleanData(historyValues);
      final Collection<String> invalidPoints = historyService.getInvalidPoints(historyValues);
      final ClientDataTagValue tagValue = tagService.getDataTagValue(Long.parseLong(id));
      
      model.addAttribute("CSV", historyService.getHistoryCSV(historyValues, isBooleanData));
      model.addAttribute("invalidPoints", invalidPoints);
      model.addAttribute("id", id);
      model.addAttribute("ylabel", tagValue.getUnit());
      model.addAttribute("tagName", tagValue.getName());
      model.addAttribute("labels", new String[]{"Server Timestamp", 
          "[" + id + "] " });

      model.addAttribute("legend", tagValue.getName());
      model.addAttribute("is_boolean", ((Boolean)(isBooleanData)));
      model.addAttribute("unit", tagValue.getUnit());
      model.addAttribute("fill_graph", true);
      
      model.addAttribute("records", lastRecords);
      
      return "trend_views/trend_view";
      
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return null;
  }
  
  /**
   * Displays a Trend View for a given id.
   * @param id the last 100 records of the given tag id are being shown
   * @param response the html result is written to that HttpServletResponse response
   * @return nothing
   * @throws IOException 
   * */
  @RequestMapping(value = TREND_VIEW_URL + "{id}", method = { RequestMethod.GET })
  public String viewTrendDefault(@PathVariable(value = "id") final String id,
      final Model model) throws IOException  {
    
    logger.info(TREND_VIEW_URL + "{id} " + id);

    try {
      final List<HistoryTagValueUpdate> historyValues = 
          historyService.requestHistoryData(id, RECORDS_TO_ASK_FOR);
      
      final boolean isBooleanData = historyService.isBooleanData(historyValues);
      final Collection<String> invalidPoints = historyService.getInvalidPoints(historyValues);
      final ClientDataTagValue tagValue = tagService.getDataTagValue(Long.parseLong(id));
      
      model.addAttribute("CSV", historyService.getHistoryCSV(historyValues, isBooleanData));
      model.addAttribute("invalidPoints", invalidPoints);
      model.addAttribute("id", id);
      model.addAttribute("ylabel", tagValue.getUnit());
      model.addAttribute("tagName", tagValue.getName());
      model.addAttribute("labels", new String[]{"Server Timestamp", 
          "[" + id + "] " });

      model.addAttribute("legend", tagValue.getName());
      model.addAttribute("is_boolean", ((Boolean)(isBooleanData)));
      model.addAttribute("unit", tagValue.getUnit());
      model.addAttribute("fill_graph", true);
      
      return "trend_views/trend_view";
      
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return null;
  }
  

  /**
   * Displays an input form for a tag id.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = TREND_VIEW_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTrendFormPost(@RequestParam(value = "id", required = false) final String id,
      final Model model) {
    
    logger.info(TREND_VIEW_FORM_URL + id);
    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TREND_FORM_TITLE, INSTRUCTION,
          TREND_VIEW_FORM_URL, null, null));
    }
    else {
      return ("redirect:" + TREND_VIEW_URL + id);
    }
    return "trend_views/trend_view_form";
  }
}