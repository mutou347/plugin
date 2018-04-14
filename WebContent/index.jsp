<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>My JSP 'index.jsp' starting page</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<script type="text/javascript" src="jquery.min.js"></script>


<body>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Document</title>
</head>
<body>
    <table border="1">
        <tr>
            <th>学号</th>
            <th>名字</th>
            <th id="sort">成绩</th>
        </tr>
        <tr>
            <td>1002</td>
            <td>小铭</td>
            <td>1012.88</td>
        </tr>
        <tr>
            <td>1003</td>
            <td>小红</td>
            <td>1053.45</td>
        </tr>
        <tr>
            <td>1004</td>
            <td>小黄</td>
            <td>1273.09</td>
        </tr>
        <tr>
            <td>1005</td>
            <td>小米</td>
            <td>993.29</td>
        </tr>
        <tr>
            <td>1006</td>
            <td>小蒋</td>
            <td>78</td>
        </tr>
        <tr>
            <td>1007</td>
            <td>小捷</td>
            <td>97</td>
        </tr>
        <tr>
            <td>1004</td>
            <td>小邓</td>
            <td>65</td>
        </tr>
    </table>
<script>
    var sort = document.getElementById('sort');
    var up = true
    sort.onclick = function(){
        var table = document.getElementsByTagName('table')[0];
        var tr = table.getElementsByTagName('tr');
        var array = [];
        for (var i = 1;i < tr.length;i++) {
            array.push(tr[i]);
        }
        if (up) {
            SortUp (array);
            up = false;
        } else {
            SortDown (array);
            up = true;
        }

        for (var i = 0; i < array.length; i++){
            table.appendChild(array[i]);
        }
    }
    function SortUp(array){
        for (var i = 0;i < array.length;i++) {
            for (var j = i + 1;j < array.length;j++) {
                if (array[j] === undefined) {
                    continue;
                }
                if (parseInt(array[i].getElementsByTagName('td')[2].innerText) <= parseInt(array[j].getElementsByTagName('td')[2].innerText)) {
                    var temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
        }
    }
    function SortDown(array){
        for (var i = 0;i < array.length;i++) {
            for (var j = i + 1;j < array.length;j++) {
                if (array[j] === undefined) {
                    continue;
                }
                if (parseInt(array[i].getElementsByTagName('td')[2].innerText) >= parseInt(array[j].getElementsByTagName('td')[2].innerText)) {
                    var temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
        }
    }
</script>
    
</body>
</html> 


