Hallo ${application.person.niceName},

${application.applier.niceName} hat deine Krankmeldung zu Urlaub umgewandelt.
Es handelt sich um folgenden Zeitraum: ${application.startDate.toString("dd.MM.yyyy")} bis ${application.endDate.toString("dd.MM.yyyy")}

Für Details siehe: ${settings.baseLinkURL}web/application/<#if application.id??>${application.id}</#if>