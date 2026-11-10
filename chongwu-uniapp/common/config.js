// 业务 API 线上备份：
const ROOTPATH = 'https://cwjpy.xianmxkj.com/sqx_fast';
const ROOTPATH3 = 'https://cwjpy.xianmxkj.com/sqx_fast';

// 业务 API 本地：
// const ROOTPATH = 'http://192.168.0.122:9294/sqx_fast';
// const ROOTPATH3 = 'http://192.168.0.122:9294/sqx_fast';

// 图片上传 / 文件访问：线上用户端域名
const UPLOADPATH = 'https://cwjpy.xianmxkj.com/sqx_fast';
// 本地上传备份：
// const UPLOADPATH = ROOTPATH;

/** H5 分享域名（无尾斜杠）；本地 H5 运行时会优先用当前 origin */
const H5HOST = 'https://cwjpy.xianmxkj.com';
/** 微信小程序 AppId（App 跳转小程序用） */
const MP_APPID = 'wx20aea1e4906a96dc';
/** 微信小程序原始 ID（gh_ 开头；App 用微信 SDK 拉起时需要，可留空走 navigateToMiniProgram） */
const MP_ORIGINAL_ID = '';

const config = {
	APIHOST: ROOTPATH,
	WSHOST1: ROOTPATH3,
	UPLOADHOST: UPLOADPATH,
	H5HOST,
	MP_APPID,
	MP_ORIGINAL_ID,
};
export default config;
