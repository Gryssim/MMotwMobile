# MMotwMobile
Mobile(Android) application for MMotw.

Initial (Main) activity used as login through rest API. 
>Connects and sends request data through JSON object and HtmlURLConnection through Java object.
>Retreives echo'd responce from PHP API hosted on connected server. 
>Checks "auth" from returned JSON string to allow login.

daily_task_activity used to display tasks retrieved from JSON string echod from PHP API
>Left and Right button (improperly) decrement/increment date and request new JSON string. 
>>TODO: Implement proper date class to update selected date.
