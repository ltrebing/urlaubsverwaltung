import { setup } from './TestSetupHelper';

describe ('calendar', () => {
    beforeEach (calendarTestSetup);

    it ('renders', () => {
        // 01.12.2017
        const referenceDate = new Date(1512130448379);

        const webPrefix = 'webPrefix';
        const apiPrefix = 'apiPrefix';
        const personId = 'personId';
        const holidayService = window.Urlaubsverwaltung.HolidayService.create();
        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);

        expect(document.body).toMatchSnapshot();
    });

    async function calendarTestSetup () {
        await setup();

        jest.spyOn(window.jQuery, 'ajax').mockReturnValue(Promise.reject());
        window.moment = await import('moment');

        document.body.innerHTML = `<div id="datepicker"></div>`;

        await import('../../main/resources/static/js/calendar.js');
    }
});
