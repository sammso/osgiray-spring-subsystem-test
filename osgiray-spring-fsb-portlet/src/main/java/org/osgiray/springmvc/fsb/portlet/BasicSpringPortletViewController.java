package org.osgiray.springmvc.fsb.portlet;

import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.DATE_TIME_PATTERN;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.DAYS_TO_BIRTHDAY_PARAM;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.GREETING;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.GREETING_VIEW;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.MAIN_VIEW;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.PARAM_VIEW;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.PERSON_PTO;
import static org.osgiray.springmvc.fsb.util.BasicSpringPortletConstants.TEST_ACTION;

import javax.portlet.ActionResponse;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.osgiray.springmvc.fsb.pto.PersonPto;
import org.osgiray.springmvc.fsb.util.JodaDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * This class is base controller for VIEW mode.
 */
@Controller
@RequestMapping("VIEW")
public class BasicSpringPortletViewController {
    protected final Logger LOG = Logger.getLogger(BasicSpringPortletViewController.class);

    @Autowired
    private PersonPtoValidator personPtoValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(DateTime.class, new JodaDateEditor(DATE_TIME_PATTERN));
    }

    @RenderMapping
    public String question(Model model) {
        LogMF.debug(LOG,"Showing form for user.",null);
        if (!model.containsAttribute(PERSON_PTO)) {
            model.addAttribute(PERSON_PTO, new PersonPto());
        }
        return MAIN_VIEW;
    }

    @RenderMapping(params = PARAM_VIEW + "=" + GREETING)
    public String greeting(@ModelAttribute(PERSON_PTO) PersonPto personPto, Model model) {
        LogMF.debug(LOG,"Showing result for user {0}.",personPto);
        Integer days = daysToBirthday(personPto.getDateOfBirth());
        model.addAttribute(DAYS_TO_BIRTHDAY_PARAM, days);
        return GREETING_VIEW;
    }


    @ActionMapping(TEST_ACTION)
    public void doAction(
            @ModelAttribute(PERSON_PTO) PersonPto personPto,
            BindingResult result,
            ActionResponse response) {
        LogMF.info(LOG,"Processing person {0}",personPto);
        personPtoValidator.validate(personPto,result);
        if (!result.hasErrors()) {
            response.setRenderParameter(PARAM_VIEW, GREETING);
        }
    }

    private Integer daysToBirthday(DateTime dateOfBirth) {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        int year = now.getYear();
        DateTime birthday = dateOfBirth.withYear(year);
        if (birthday.isBefore(now)) {
            birthday = birthday.plusYears(1);
        }
        return Days.daysBetween(now, birthday).getDays();
    }
}