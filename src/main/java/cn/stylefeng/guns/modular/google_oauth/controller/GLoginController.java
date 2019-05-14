package cn.stylefeng.guns.modular.google_oauth.controller;

import cn.stylefeng.guns.core.util.HttpClientUtil;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName: LoginController
 * @Description: Google第三方登录
 * @param
 * @date Mar 13, 2017
 */
@Controller
public class GLoginController {

    private static String client_id = "693702919517-g5jgaqdt3et1k81kh8k1otgdlkuvkf04.apps.googleusercontent.com";
    private static String client_secret = "JupmsoPgdrm9ph9dhkC-wq25";
    private static String scope = "https://www.googleapis.com/auth/drive.metadata.readonly";
    private static String redirect_url = "http://localhost:8081/GoogleUserInfo";
    private static String code_url = "https://accounts.google.com/o/oauth2/v2/auth";
    private static String token_url = "https://www.googleapis.com/oauth2/v4/token";
    private static String user_url = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static String verify_url = "https://www.googleapis.com/oauth2/v3/tokeninfo";

    //第一步：跳转到登录页面
    @RequestMapping(value = "/login")
    public String toIndex(HttpServletRequest request) {
        return "google";
    }

    /**
     * @Title: Login
     * @Description: google登录验证后会重定向到此地址，并附带访问授权码，不能为公开的ip地址
     * @author 此方法是用带回的code换取accessToken，然后用accessToken换取用户信息，这个地址就是在创建应用时定义的重定向地址
     * @return Object
     * @date Mar 23, 2017 10:37:38 AM
     * @throws
     */
    @RequestMapping(value = "/GoogleUserInfo")
    @ResponseBody
    public static Object Login(HttpServletRequest request) {
        String code = request.getParameter("code");
        System.out.println(code);

//		String idToken = getGoogleAccessToken(code);
//		System.out.println(idToken);
//		JSONObject verifyToken = verifyToken(idToken);
//		System.out.println(verifyToken);

        String accessToken = getGoogleAccessToken(code);
        System.out.println(accessToken);
        JSONObject userInfo = getUserInfo(accessToken);
        System.out.println(userInfo);
        return userInfo;
    }

    /**
     * @throws Exception
     * @Title: sendRedirect
     * @Description:发送授权请求
     * @author 第二步，在google.jsp中用户登录成功以后回跳转到这个路径，发送请求让用户授权，授权成功后重定向到/GoogleUserInfo，也就是创建应用时定义的重定向地址
     * @return String
     * @date Mar 24, 2017 3:11:36 PM
     * @throws
     */
    @RequestMapping(value = "/sendRedirect")
    public void sendRedirect(HttpServletResponse response) throws Exception {
        // 随机字符串，防止csrf攻击
        String state = UUID.randomUUID() + "";
        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", client_id);
        params.put("redirect_uri", redirect_url);
        params.put("response_type", "code");
        params.put("scope", scope);
        params.put("access_type", "offline");
        params.put("state", state);
        params.put("include_granted_scopes", "true");
        String url = code_url+"?"+ "client_id=" +client_id+
                                   "&redirect_uri="+ redirect_url+
                                   "&response_type=code" +
                                   "&scope="+ scope+
                                   "&access_type=offline"+
                                   "&state="+ state+
                                   "&include_granted_scopes=true";
        //String url = HttpClientUtil.getUrl(code_url, params);
        response.sendRedirect(url);
    }

    /**
     * @Title: getGoogleAccessToken
     * @Description: 获取accessToken
     * @author 第三步，用重定向带回来的code换取accessToken
     * @return String
     * @date Mar 25, 2017 10:25:00 AM
     * @throws
     */
    public static String getGoogleAccessToken(String code) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("client_id", client_id);
        params.put("redirect_uri", redirect_url);
        params.put("client_secret", client_secret);
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        String httpClientResult = null;
        String accessToken =null;
        String idToken=null;
        try {
            httpClientResult = HttpClientUtil.doPost(token_url, params, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != httpClientResult ) {
            //String result = httpClientResult.getContent();
            String result = httpClientResult;
            JSONObject jsonObject = JSONObject.fromObject(result);
            accessToken = jsonObject.getString("access_token");
            idToken=jsonObject.getString("id_token");
        }
        return accessToken;
//		return idToken;
    }

    /**
     * @Title: getUserInfo
     * @Description: 获取用户信息
     * @author第四步，用accessToken获取用户信息
     * @return String
     * @date Mar 25, 2017 11:50:23 AM
     * @throws
     */
    public static JSONObject getUserInfo(String accessToken) {
        HashMap<String, String> params = new HashMap<String,String>();
        params.put("access_token", accessToken);
        String responseResult =null;
        JSONObject userInfo=null;
        try {
            responseResult = HttpClientUtil.doGet(user_url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != responseResult) {
            String result = responseResult;
            userInfo =  JSONObject.fromObject(result);
        }
        return userInfo;
    }

    /**
     * @Title: verifyToken
     * @Description:验证用户token是否是来自本应用的请求，校验aud和clientID是否相同
     * @author第五步，验证用户是否来自你的应用，防刷，根据需要加到逻辑里
     * @return String
     * @date Mar 25, 2017 7:36:33 PM
     * @throws
     */
    public static JSONObject verifyToken(String idToken){
        HashMap<String, String> params = new HashMap<String,String>();
        params.put("id_token", idToken);
        String responseResult =null;
        JSONObject verifyInfo=null;
        try {
            responseResult = HttpClientUtil.doGet(verify_url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != responseResult) {
            String result = "";
        verifyInfo =  JSONObject.fromObject(result);
    }
        return verifyInfo;
    }

}
