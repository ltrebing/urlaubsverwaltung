import $ from 'jquery'
import 'tablesorter'
import '../css/vacation-overview.css'

$(function () {

    function selectedItemChange() {
      const selectedYear = document.querySelector('#yearSelect');
      const selectedMonth = document.querySelector('#monthSelect');
      const selectedDepartment = document.querySelector('#departmentSelect');
      const selectedDepartmentValue = selectedDepartment.options[selectedDepartment.selectedIndex].text;
      const selectedYearValue = selectedYear.options[selectedYear.selectedIndex].text;
      const selectedMonthValue = selectedMonth.options[selectedMonth.selectedIndex].value;

      function compare(currentDay, currentValue, status, type, dayLength) {
        if (currentDay.dayText === currentValue.date
          && currentValue.status === status
          && currentValue.type === type
          && currentValue.dayLength === dayLength) {
          return true;
        }
      }

      if (selectedYearValue != null && selectedMonthValue != null && selectedDepartmentValue != null) {
        const url = location.protocol + "//" + location.host
          + "/api/vacationoverview?selectedYear="
          + encodeURIComponent(selectedYearValue) + "&selectedMonth="
          + encodeURIComponent(selectedMonthValue) + "&selectedDepartment="
          + encodeURIComponent(selectedDepartmentValue);

        const xhttp = new XMLHttpRequest();
        xhttp.open("GET", url, false);
        xhttp.setRequestHeader("Content-type", "application/json");
        xhttp.send();
        const holyDayOverviewResponse = JSON.parse(xhttp.responseText);
        if (holyDayOverviewResponse && holyDayOverviewResponse.response) {

          const overViewList = holyDayOverviewResponse.response.list;
          overViewList
            .forEach(function (listItem) {
              const personId = listItem.personID;
              const url = location.protocol + "//"
                + location.host + "/api/absences?year="
                + encodeURIComponent(selectedYearValue) + "&month="
                + encodeURIComponent(selectedMonthValue) + "&person="
                + encodeURIComponent(personId);
              const xhttp = new XMLHttpRequest();
              xhttp.open("GET", url, false);
              xhttp.setRequestHeader("Content-type",
                "application/json");
              xhttp.send();

              const response = JSON.parse(xhttp.responseText);
              if (response) {

                listItem.days
                  .forEach(
                    function (currentDay) {
                      let absences = response.response.absences;

                      if (absences.find(currentValue => compare(currentDay, currentValue, "WAITING", "VACATION", 1))) {
                        currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING vacationOverview-day-item';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "WAITING", "VACATION", 0.5))) {
                        currentDay.cssClass = 'vacationOverview-day-personal-holiday-half-day-status-WAITING vacationOverview-day-item';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ALLOWED", "VACATION", 0.5))) {
                        currentDay.cssClass = 'vacationOverview-day-personal-holiday-half-day-status-ALLOWED vacationOverview-day-item';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ALLOWED", "VACATION", 1))) {
                        currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-ALLOWED vacationOverview-day-item';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ACTIVE", "SICK_NOTE", 1))) {
                        currentDay.cssClass = 'vacationOverview-day-sick-note vacationOverview-day-item';
                      }

                      if (absences.find(currentValue => compare(currentDay, currentValue, "ACTIVE", "SICK_NOTE", 0.5))) {
                        currentDay.cssClass = 'vacationOverview-day-sick-note-half-day vacationOverview-day-item';
                      }

                    }, this);
              }
            });

          let outputTable = "<table cellspacing='0' id='vacationOverviewTable' class='list-table sortable tablesorter vacationOverview-table'>";
          outputTable += "<thead class='hidden-xs'>";
          outputTable += "<tr>";
          outputTable += "<th class='sortable-field'><spring:message code='person.data.firstName'/></th>";
          outputTable += "<th class='sortable-field'><spring:message code='person.data.lastName'/></th>";
          overViewList[0].days
            .forEach(
              function (item) {
                let defaultClasses = "non-sortable vacationOverview-day-item";
                if (item.typeOfDay === "WEEKEND") {
                  outputTable += "<th class='" + defaultClasses + "vacationOverview-day-weekend'>" + item.dayNumber + "</th>";
                } else {
                  outputTable += "<th class='" + defaultClasses + "'>" + item.dayNumber + "</th>";
                }
              }, outputTable);
          outputTable += "</tr><tbody class='vacationOverview-tbody'>";
          overViewList
            .forEach(
              function (item) {
                outputTable += "<tr>";
                outputTable += "<td class='hidden-xs'>"
                  + item.person.firstName
                  + "</td>";
                outputTable += "<td class='hidden-xs'>"
                  + item.person.lastName
                  + "</td>";
                item.days
                  .forEach(
                    function (dayItem) {
                      if (dayItem.typeOfDay === "WEEKEND") {
                        dayItem.cssClass = ' vacationOverview-day-weekend vacationOverview-day-item';
                      } else {
                        if (!dayItem.cssClass) {
                          dayItem.cssClass = ' vacationOverview-day vacationOverview-day-item ';
                        }
                      }
                      outputTable += "<td class='" + dayItem.cssClass + "'></td>";
                    }, outputTable);
                outputTable += "</tr>";
              }, outputTable);

          outputTable += "</tbody></table>";
          const element = document.querySelector("#vacationOverview");
          element.innerHTML = outputTable;
        }
      }

      $("table.sortable").tablesorter({
        sortList: [[0, 0]],
        headers: {
          '.non-sortable': {
            sorter: false
          }
        }
      });
    }

    const selectedYear = document.querySelector('#yearSelect');
    const selectedMonth = document.querySelector('#monthSelect');
    const selectedDepartment = document.querySelector('#departmentSelect');

    selectedYear.addEventListener("change", function () {
      selectedItemChange();
    });
    selectedMonth.addEventListener("change", function () {
      selectedItemChange();
    });
    selectedDepartment.addEventListener("change", function () {
      selectedItemChange();
    });

    let event;
    if (typeof (Event) === "function") {
      event = new Event("change");
    } else {
      event = document.createEvent("Event");
      event.initEvent("change", true, true);
    }
    selectedYear.dispatchEvent(event);
  }
);
