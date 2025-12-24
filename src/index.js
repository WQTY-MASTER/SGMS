// ...existing code...
async function handleLogin(credentials) {
    try {
        const response = await axios.post('/api/auth/login', credentials);
        console.log('登录接口返回数据: ', response);

        // 检查接口返回的数据
        if (response.data && response.data.code === 200) {
            const token = response.data.data?.token; // 假设 token 在 data.token 中
            if (token) {
                // 存储 token
                localStorage.setItem('authToken', token);
                console.log('登录成功，凭证已存储');
                // ...其他登录成功后的逻辑...
            } else {
                console.error('登录失败：未获取到登录凭证');
                throw new Error('登录失败：未获取到登录凭证');
            }
        } else {
            console.error('登录失败，服务器返回错误: ', response.data?.msg || '未知错误');
            throw new Error(response.data?.msg || '登录失败：未知错误');
        }
    } catch (error) {
        console.error('登录错误详情: ', error);
        throw error;
    }
}
// ...existing code...
