<#-- @ftlvariable name="timestamp" type="java.lang.String" -->
<#-- @ftlvariable name="username" type="java.lang.String" -->
<#-- @ftlvariable name="pictures" type="java.util.List" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->
<#-- @ftlvariable name="avatar" type="java.lang.String" -->
<#-- @ftlvariable name="background" type="java.lang.String" -->
<html>
<head>
    <meta charset="UTF-8"/>
    <style>
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
            background-color: ${background!'#F6F6F6'};
            font-family: "Sarasa Mono SC", monospace;
        }

        .card {
            width: 700px;
            display: flex;
            flex-direction: row;
            min-height: 200px;
            border-radius: 10px;
            background-color: white;
            box-shadow: 7px 7px 4px rgba(0, 0, 0, 0.012),
            22px 22px 13px rgba(0, 0, 0, 0.018),
            100px 100px 60px rgba(0, 0, 0, 0.03);
        }

        .card-avatar-container {
            padding: 20px;
            flex: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .card-avatar {
            width: 70px;
            height: 70px;
            object-fit: cover;
            border-radius: 100%;
        }

        .card-body {
            flex: 1;
            display: flex;
            padding: 30px 20px 30px 0;
            flex-direction: column;
        }

        .card-title {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .card-name {
            font-size: 20px;
        }

        .card-time {
            font-size: 13px;
            color: #959595;
            margin-right: 10px;
        }

        .card-content {
            padding: 10px 20px;
            background-color: #F2F2F2;
            border-radius: 10px;
            line-height: 1.5;
        }

        .card-footer {
            margin-top: 10px;
            font-size: 13px;
            color: #959595;
            display: flex;
            justify-content: start;
        }
    </style>
</head>
<body>
<div class="card">
    <div class="card-avatar-container">
        <img class="card-avatar" src="${avatar}" alt="#"/>
    </div>
    <div class="card-body">
        <div class="card-title">
            <div class="card-name">${username}</div>
            <div class="card-time">${timestamp}</div>
        </div>
        <div class="card-content">
            ${content}
            <br/>
            <#list pictures as picture >
                <img style="margin-top: 10px" src="${picture}" alt="#"/>
            </#list>
        </div>
        <div class="card-footer">
            -- 由果酱生成 --
        </div>
    </div>
</div>
</body>
</html>
