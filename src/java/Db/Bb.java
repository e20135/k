/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Db;

import beans.ItemEJB;
import entities.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
//import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseId;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import static javax.mail.internet.HeaderTokenizer.Token.EOF;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import static jdk.nashorn.tools.ShellFunctions.input;
import net.tkxtools.MailSender;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.copy;
import org.hibernate.validator.constraints.NotEmpty;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import util.PagenationHelper;

/**
 *
 * @author Masanari
 * @param <T>
 */
@ManagedBean(name = "bb", eager = true)
@SessionScoped
public class Bb extends SuperBb implements Serializable {

    SuperBb sbb;
//
//    private static final long serialVersionUID = 1L;
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)エンティティでIDを自動作成するためここではヌルにしておく。
//    @NotEmpty
    private Integer id;
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String firstNameHurigana;
    @NotEmpty
    private String lastNameHurigana;
    @NotEmpty
    private String addressOne;

    private String addressTwo;
    @NotEmpty
    private String birthYear = "1940";
    @NotEmpty
    private String birthMonth = "1";
    @NotEmpty
    private String birthDay = "1";
    @NotEmpty
    private String firstNameWife;
    @NotEmpty
    private String lastNameWife;
    @NotEmpty
    private String firstNameHuriganaWife;
    @NotEmpty
    private String lastNameHuriganaWife;
    @NotEmpty
    private String birthYearWife = "1940";
    @NotEmpty
    private String birthMonthWife = "1";
    @NotEmpty
    private String birthDayWife = "1";
    @NotEmpty
    private String phoneNumber;

    private String educationContent;

    private String message;

    private Integer payment;

    private Part file;

    private Part fileRoom;

    InputStream is;

    private byte[] image;

    private byte[] imageRoom;

    private String previous = "前へ";

    private String next = "次へ";

    private boolean listFlag;

    @NotEmpty
    private String mailAddress;

    String detail = "詳細へ";

    protected OldCoupleInformation oci;

    @EJB
    protected OldCoupleInformationDb db;
    @Inject
    transient Logger log;
    @EJB
    protected MailSender sender;		// 電子メールユーティリティ
    @Inject
    protected MakeText text;
//OldCoupleInformationをEJBで持ってこようとすると失敗するのはなんでだ

//confirmBeanを作って、そこで再びflashによるデータの受け渡しをするとうまくいくかもしれない。
    @ManagedProperty(value = "#{dbbean}")
    private DbBean dbbean;

    protected List<OldCoupleInformation> coupleList;
    private String search;

    public void Id() {
        Flash flash = FacesContext.getCurrentInstance()
                .getExternalContext().getFlash();
        this.coupleList = (List) flash.get("coupleList");
    }

    @PostConstruct
    public void filt() {
        if (FacesContext.getCurrentInstance().isPostback()) {
            System.out.println("this is postBack.");
            coupleList = db.getAll();
//        System.out.println(id);idを出力しようとするとerrorになるのはなぜだ
        } else {
            System.out.println("filt()");
//            coupleList = db.getAll();
            coupleList = db.filterTable(search);
            System.out.println("coupleList = db.filterTable(search);\n");
        }
        
    }

    public void search() {
        coupleList = db.filterTable(search);
        System.out.println("coupleList = db.filterTable(search);\n");
    }

    public String detail(Integer id) {
        System.out.println("detail()");
        oci = db.find(id);
        return "detail-content.xhtml";
    }

    public String goToContract(Integer id, String firstName, String addressOne, String addressTwo, String payment, String educationContent, String mailAddress, String phoneNumber) {
        Flash flash = FacesContext.getCurrentInstance()
                .getExternalContext().getFlash();
        System.out.println(firstName);
        flash.put("id", id);
        flash.put("firstName", firstName);
        flash.put("birthYear", birthYear);
        flash.put("birthDay", birthDay);
        flash.put("birthMonth", birthMonth);
        flash.put("addressOne", addressOne);
        flash.put("addressTwo", addressTwo);
        flash.put("educationContent", educationContent);
        flash.put("payment", payment);
        flash.put("mailAddress", mailAddress);
        flash.put("phoneNumber", phoneNumber);//電話番号とアドレスは画面には表示しないでメールでこちらに送信するために値を受けわたす
        return "index-parent.xhtml";
    }

    
    public StreamedContent getPic() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        } else {
            ExternalContext sv = context.getExternalContext();
            Map<String, String> map = sv.getRequestParameterMap();
            String key = map.get("id");
            OldCoupleInformation e = find(Integer.valueOf(key));

            ByteArrayInputStream in = new ByteArrayInputStream(e.getImage());
            DefaultStreamedContent ds = new DefaultStreamedContent(in);
            return ds;
        }
    }

    public StreamedContent getRoomPic() {

        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        } else {
            ExternalContext sv = context.getExternalContext();
            Map<String, String> map = sv.getRequestParameterMap();
            String key = map.get("id");
            OldCoupleInformation e = find(Integer.valueOf(key));

            ByteArrayInputStream in = new ByteArrayInputStream(e.getImageRoom());
            DefaultStreamedContent ds = new DefaultStreamedContent(in);
            return ds;
        }
    }

    public ExternalContext getServlet() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    /* *****（リクエストオブジェクトを取得する）**************/
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) getServlet().getRequest();
    }

//書籍エンティティのリスト
    public byte[] toByteArray(Part file) throws IOException {
        if (file != null) {
            byte[] data = new byte[(int) file.getSize()];   // byte配列を作成

            InputStream in = file.getInputStream();     // ストリームからbyte配列
            in.read(data);
            return data;
// に、入力する

        } else {
            System.out.println("file = null");
            return null;
        }
    }

//        return output.toByteArray();
//        byte[] data = new byte[(int) file.getSize()];   // byte配
    public void imageToByte() throws IOException {
        if (this.file != null) {
            image = toByteArray(this.file);
            imageRoom = toByteArray(this.fileRoom);
        } else {
            System.out.println("file is null");
        }

    }

    public String goToConfirm() throws IOException {
        imageToByte();
        Flash flash = FacesContext.getCurrentInstance()
                .getExternalContext().getFlash();
        flash.put("firstName", this.firstName);
        flash.put("lastName", this.lastName);
        flash.put("firstNameHurigana", this.firstNameHurigana);
        flash.put("lastNameHurigana", this.lastNameHurigana);
        flash.put("addressOne", this.addressOne);
        flash.put("addressTwo", this.addressTwo);
        flash.put("birthYear", this.birthYear);
        flash.put("birthMonth", this.birthMonth);
        flash.put("birthDay", this.birthDay);
        flash.put("firstNameWife", this.firstNameWife);
        flash.put("lastNameWife", this.lastNameWife);
        flash.put("firstNameHuriganaWife", this.firstNameHuriganaWife);
        flash.put("lastNameHuriganaWife", this.lastNameHuriganaWife);
        flash.put("birthYearWife", this.birthYearWife);
        flash.put("birthMonthWife", this.birthMonthWife);
        flash.put("birthDayWife", this.birthDayWife);
        flash.put("phoneNumber", this.phoneNumber);
        flash.put("mailAddress", this.mailAddress);
        flash.put("message", this.message);
        flash.put("educationContent", this.educationContent);
        flash.put("payment", this.payment);
        flash.put("image", image);
        flash.put("imageRoom", imageRoom);

        return "/confirm.xhtml?faces-redirect=true";
    }

//        public String edit(Employee employee) {	// 編集データのセット
//        number = employee.getNumber();
//        name = employee.getName();
//        mail = employee.getMail();
//        return null;
//    }


    public void sendMail() {
        sender.send(this.mailAddress, "お問い合わせのご確認", text.getText(this.firstName));
    }

    public List<OldCoupleInformation> getAll() {
        return db.getAll();
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void setId() {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstNameHurigana() {
        return firstNameHurigana;
    }

    public void setFirstNameHurigana(String firstNameHurigana) {
        this.firstNameHurigana = firstNameHurigana;
    }

    public String getLastNameHurigana() {
        return lastNameHurigana;
    }

    public void setLastNameHurigana(String lastNameHurigana) {
        this.lastNameHurigana = lastNameHurigana;
    }

    public String getAddressOne() {
        return addressOne;
    }

    public void setAddressOne(String addressOne) {
        this.addressOne = addressOne;
    }

    public String getAddressTwo() {
        return addressTwo;
    }

    public void setAddressTwo(String addressTwo) {
        this.addressTwo = addressTwo;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getFirstNameWife() {
        return firstNameWife;
    }

    public void setFirstNameWife(String firstNameWife) {
        this.firstNameWife = firstNameWife;
    }

    public String getLastNameWife() {
        return lastNameWife;
    }

    public void setLastNameWife(String lastNameWife) {
        this.lastNameWife = lastNameWife;
    }

    public String getFirstNameHuriganaWife() {
        return firstNameHuriganaWife;
    }

    public void setFirstNameHuriganaWife(String firstNameHuriganaWife) {
        this.firstNameHuriganaWife = firstNameHuriganaWife;
    }

    public String getLastNameHuriganaWife() {
        return lastNameHuriganaWife;
    }

    public void setLastNameHuriganaWife(String lastNameHuriganaWife) {
        this.lastNameHuriganaWife = lastNameHuriganaWife;
    }

    public String getBirthYearWife() {
        return birthYearWife;
    }

    public void setBirthYearWife(String birthYearWife) {
        this.birthYearWife = birthYearWife;
    }

    public String getBirthMonthWife() {
        return birthMonthWife;
    }

    public void setBirthMonthWife(String birthMonthWife) {
        this.birthMonthWife = birthMonthWife;
    }

    public String getBirthDayWife() {
        return birthDayWife;
    }

    public void setBirthDayWife(String birthDayWife) {
        this.birthDayWife = birthDayWife;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public MailSender getSender() {
        return sender;
    }

    public void setSender(MailSender sender) {
        this.sender = sender;
    }

    public MakeText getText() {
        return text;
    }

    public void setText(MakeText text) {
        this.text = text;
    }

    public Bb() {
    }

    public void setDbbean(DbBean dbbean) {
        this.dbbean = dbbean;
    }

    public DbBean getDbbean() {
        return dbbean;
    }

    public String getEducationContent() {
        return educationContent;
    }

    public void setEducationContent(String educationContent) {
        this.educationContent = educationContent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OldCoupleInformationDb getDb() {
        return db;
    }

    public void setDb(OldCoupleInformationDb db) {
        this.db = db;
    }

    public Integer getPayment() {
        return payment;
    }

    public void setPayment(Integer payment) {
        this.payment = payment;
    }

    public OldCoupleInformation getOci() {
        return oci;
    }

    public void setOci(OldCoupleInformation oci) {
        this.oci = oci;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImageRoom() {
        return imageRoom;
    }

    public void setImageRoom(byte[] imageRoom) {
        this.imageRoom = imageRoom;
    }

    public Part getFileRoom() {
        return fileRoom;
    }

    public void setFileRoom(Part fileRoom) {
        this.fileRoom = fileRoom;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

//
//    public SearchBb getSearchBb() {
//        return searchBb;
//    }
//
//    public void setSearchBb(SearchBb searchBb) {
//        this.searchBb = searchBb;
//    }
    public boolean isListFlag() {
        return listFlag;
    }

    public void setListFlag(boolean listFlag) {
        this.listFlag = listFlag;
    }

    public List<OldCoupleInformation> getCoupleList() {
        return coupleList;
    }

    public void setCoupleList(List<OldCoupleInformation> coupleList) {
        this.coupleList = coupleList;
    }

}
