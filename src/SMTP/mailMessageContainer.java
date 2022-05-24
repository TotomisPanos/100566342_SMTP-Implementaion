package SMTP;

public class mailMessageContainer {
    public String date_container;
    public String from_container;
    public String to_container;
    public String subject_container;
    public String mail_body_container;
    
    public mailMessageContainer(){}
    
    public mailMessageContainer(String mailDate, String mailFrom, String mailTo, String mailSubject, String mailBody){
        date_container = mailDate;
        from_container = mailFrom;
        to_container = mailTo;
        subject_container = mailSubject;
        mail_body_container = mailBody;
    }
    
    public static void main(String[] args) {
        mailMessageContainer self = new mailMessageContainer("test date","test from","test to","test subject","test body");
        System.out.println(self.date_container);
        System.out.println(self.from_container);
        System.out.println(self.to_container);
        System.out.println(self.subject_container);
        System.out.println(self.mail_body_container);
    }
}
