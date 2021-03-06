package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonConfigurationProperties;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PersonManagementViewControllerTest {

    private PersonManagementViewController sut;

    private static final int PERSON_ID = 1;
    private static final int UNKNOWN_PERSON_ID = 675;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonValidator validator;

    private PersonConfigurationProperties personConfigurationProperties = new PersonConfigurationProperties();

    @BeforeEach
    void setUp() {

        sut = new PersonManagementViewController(personService, departmentService, validator, personConfigurationProperties);
    }

    @Test
    void newPersonFormUsesNewPerson() throws Exception {

        perform(get("/web/person/new"))
            .andExpect(model().attribute("person", hasProperty("new", equalTo(Boolean.TRUE))));
    }

    @Test
    void newPersonFormUsesNewPersonUsesCorrectView() throws Exception {

        perform(get("/web/person/new")).andExpect(view().name("person/person_form"));
    }

    @Test
    void newPersonForwardsToViewIfValidationFails() throws Exception {

        personConfigurationProperties.setCanBeManipulated(true);

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "invalid.email");

            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person")).andExpect(view().name("person/person_form"));
    }

    @Test
    void newPersonCreatesPersonCorrectly() throws Exception {

        personConfigurationProperties.setCanBeManipulated(true);

        when(personService.create(any())).thenReturn(personWithId(PERSON_ID));

        perform(post("/web/person")
            .param("username", "username")
            .param("lastName", "Meier")
            .param("firstName", "Nina")
            .param("email", "nina@inter.net"));

        Person personWithExpectedAttributes = new Person("username", "Meier", "Nina", "nina@inter.net");

        verify(personService).create(refEq(personWithExpectedAttributes));
    }

    @Test
    void newPersonAddsFlashAttribute() throws Exception {

        personConfigurationProperties.setCanBeManipulated(true);

        when(personService.create(any())).thenReturn(personWithId(PERSON_ID));

        perform(post("/web/person")).andExpect(flash().attribute("createSuccess", true));
    }

    @Test
    void newPersonRedirectsToCreatedPerson() throws Exception {

        personConfigurationProperties.setCanBeManipulated(true);

        when(personService.create(any())).thenReturn(personWithId(PERSON_ID));

        perform(post("/web/person"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/person/" + PERSON_ID));
    }

    @Test
    void newPersonRedirectsToPersonOverviewIfCreationIsNotAllowed() throws Exception {

        perform(post("/web/person"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/person"));
    }

    @Test
    void editPersonFormUsesPersonsWithGivenId() throws Exception {

        final Person personWithGivenId = personWithId(PERSON_ID);

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithGivenId));

        perform(get("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(model().attribute("person", personWithGivenId));
    }

    @Test
    void editPersonFormForUnknownIdThrowsUnknownPersonException() {

        assertThatThrownBy(() ->
            perform(get("/web/person/" + UNKNOWN_PERSON_ID + "/edit"))
        ).hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editPersonFormAddsDepartmentsToModel() throws Exception {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        List<Department> departments = Collections.singletonList(new Department());
        List<Department> secondStageDepartments = Collections.singletonList(new Department());

        when(departmentService.getManagedDepartmentsOfDepartmentHead(any())).thenReturn(departments);
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(any())).thenReturn(secondStageDepartments);

        perform(get("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(model().attribute("departments", departments))
            .andExpect(model().attribute("secondStageDepartments", secondStageDepartments));
    }

    @Test
    void editPersonFormUsesNewPersonUsesCorrectView() throws Exception {

        when(personService.getPersonByID(PERSON_ID)).thenReturn(Optional.of(personWithId(PERSON_ID)));

        perform(get("/web/person/" + PERSON_ID + "/edit")).andExpect(view().name("person/person_form"));
    }

    @Test
    void editPersonForwardsToViewIfValidationFails() throws Exception {

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "invalid.email");

            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/" + PERSON_ID + "/edit")).andExpect(view().name("person/person_form"));
    }

    @Test
    void editPersonUpdatesPersonCorrectly() throws Exception {

        perform(post("/web/person/" + PERSON_ID + "/edit")
            .param("username", "username")
            .param("lastName", "Meier")
            .param("firstName", "Nina")
            .param("email", "nina@inter.net"));

        Person personWithExpectedAttributes = new Person("username", "Meier", "Nina", "nina@inter.net");

        verify(personService).update(refEq(personWithExpectedAttributes));
    }

    @Test
    void editPersonAddsFlashAttribute() throws Exception {

        perform(post("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(flash().attribute("updateSuccess", true));
    }

    @Test
    void editPersonRedirectsToUpdatedPerson() throws Exception {

        perform(post("/web/person/" + PERSON_ID + "/edit"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/person/" + PERSON_ID));
    }

    private static Person personWithId(int personId) {

        Person person = new Person();
        person.setId(personId);
        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(sut).build().perform(builder);
    }

}
