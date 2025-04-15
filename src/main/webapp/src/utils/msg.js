import { ElNotification } from "element-plus";

// 通知设置
const infoMsg = msg => {
  ElNotification({
    title: "通知",
    message: msg,
    type: "info",
  });
};

const successMsg = msg => {
  ElNotification({
    title: "成功",
    message: msg,
    type: "success",
  });
};

const warnMsg = msg => {
  ElNotification({
    title: "警告",
    message: msg,
    type: "warning"
  })
}

const errorMsg = msg => {
  ElNotification({
    title: "错误",
    message: msg,
    type: "error"
  })
}

export {
    infoMsg,
    successMsg,
    warnMsg,
    errorMsg
}