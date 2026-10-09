const app = getApp();

// 将 UTF-8 字节数组解码为字符串（处理跨分块的多字节字符）
function utf8Decode(bytes) {
    let encoded = '';
    for (let i = 0; i < bytes.length; i++) {
        encoded += '%' + ('00' + bytes[i].toString(16)).slice(-2);
    }
    try {
        return decodeURIComponent(encoded);
    } catch (e) {
        return '';
    }
}

function escapeHtml(text) {
    return String(text == null ? '' : text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// 轻量 Markdown -> HTML（供 rich-text 渲染）：标题/加粗/斜体/行内代码/代码块/有序无序列表/引用
function markdownToHtml(markdown) {
    if (!markdown) return '';
    const lines = String(markdown).split('\n');
    let html = '';
    let listType = '';
    let inCode = false;
    let codeBuffer = [];

    const closeList = () => {
        if (listType) {
            html += '</' + listType + '>';
            listType = '';
        }
    };

    const inline = (text) => {
        let result = escapeHtml(text);
        result = result.replace(/`([^`]+)`/g,
            '<code style="background:#f3f0ea;padding:1px 6px;border-radius:4px;font-family:monospace;">$1</code>');
        result = result.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        result = result.replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>');
        result = result.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" style="color:#9F6B65;">$1</a>');
        return result;
    };

    for (let i = 0; i < lines.length; i++) {
        const raw = lines[i];
        const trimmed = raw.trim();

        if (/^```/.test(trimmed)) {
            if (inCode) {
                html += '<div style="background:#f3f0ea;padding:12px;border-radius:8px;font-family:monospace;font-size:24rpx;white-space:pre-wrap;margin:6px 0;">'
                    + escapeHtml(codeBuffer.join('\n')) + '</div>';
                codeBuffer = [];
                inCode = false;
            } else {
                closeList();
                inCode = true;
            }
            continue;
        }
        if (inCode) {
            codeBuffer.push(raw);
            continue;
        }
        if (!trimmed) {
            closeList();
            html += '<div style="height:8px;"></div>';
            continue;
        }

        let match;
        if ((match = trimmed.match(/^(#{1,6})\s+(.*)$/))) {
            closeList();
            const size = Math.max(30 - (match[1].length - 1) * 4, 26);
            html += '<div style="font-weight:bold;font-size:' + size + 'rpx;margin:10px 0 6px;color:#5a3d38;">'
                + inline(match[2]) + '</div>';
            continue;
        }
        if ((match = trimmed.match(/^[-*+]\s+(.*)$/))) {
            if (listType !== 'ul') {
                closeList();
                html += '<ul style="padding-left:36rpx;margin:4px 0;">';
                listType = 'ul';
            }
            html += '<li style="margin:2px 0;">' + inline(match[1]) + '</li>';
            continue;
        }
        if ((match = trimmed.match(/^\d+\.\s+(.*)$/))) {
            if (listType !== 'ol') {
                closeList();
                html += '<ol style="padding-left:40rpx;margin:4px 0;">';
                listType = 'ol';
            }
            html += '<li style="margin:2px 0;">' + inline(match[1]) + '</li>';
            continue;
        }
        if ((match = trimmed.match(/^>\s?(.*)$/))) {
            closeList();
            html += '<div style="border-left:6rpx solid #9F6B65;padding-left:16rpx;color:#6b6b6b;margin:6px 0;">'
                + inline(match[1]) + '</div>';
            continue;
        }

        closeList();
        html += '<div style="margin:3px 0;line-height:1.6;">' + inline(trimmed) + '</div>';
    }

    closeList();
    if (inCode) {
        html += '<div style="background:#f3f0ea;padding:12px;border-radius:8px;font-family:monospace;white-space:pre-wrap;">'
            + escapeHtml(codeBuffer.join('\n')) + '</div>';
    }
    return html;
}

function buildMessage(role, content) {
    const text = content == null ? '' : content;
    return {
        role,
        content: text,
        html: role === 'user' ? escapeHtml(text) : markdownToHtml(text)
    };
}

Page({
    data: {
        messages: [],
        inputValue: '',
        loading: false,
        scrollToId: '',
        quickPrompts: ['推荐我的专属路线', '福州一日游怎么玩', '福建有哪些国家级非遗', '泉州非遗打卡推荐']
    },

    onLoad() {
        this.msgSeq = 0;
        this.byteBuffer = [];
        this.streaming = false;
        this.streamingIndex = null;
        this.streamingContent = '';
        this.currentTask = null;
        this.pushMessage('assistant', '你好，我是「古韵闽风」福建文旅 AI 助手。我只回答福建旅游、非遗、景点和路线规划相关的问题，还能结合你的收藏、浏览与偏好为你推荐专属路线。请问你想去哪里？');
    },

    onUnload() {
        if (this.currentTask && this.currentTask.abort) {
            this.currentTask.abort();
        }
    },

    onInput(e) {
        this.setData({ inputValue: e.detail.value });
    },

    onQuickTap(e) {
        if (this.data.loading) return;
        this.setData({ inputValue: e.currentTarget.dataset.text });
        this.onSend();
    },

    onSend() {
        const text = (this.data.inputValue || '').trim();
        if (!text || this.data.loading) return;

        // 历史对话（不含本轮问题），最多保留最近 10 条
        const history = this.data.messages
            .slice(-10)
            .map(message => ({ role: message.role, content: message.content }));

        this.setData({ inputValue: '' });
        this.pushMessage('user', text);
        this.appendStreamingMessage();

        this.byteBuffer = [];
        this.streaming = true;
        this.streamingContent = '';
        this.setData({ loading: true });

        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {};
        const userId = userInfo.userId || null;
        const url = app.globalData.baseUrl + '/AncientCharmOfFujianStyle/ai/chat';

        const task = wx.request({
            url,
            method: 'POST',
            header: { 'content-type': 'application/json' },
            enableChunked: true,
            data: { userId, message: text, history },
            success: () => this.finishStreaming(),
            fail: (err) => this.onStreamError(err)
        });

        if (task && task.onChunkReceived) {
            task.onChunkReceived((res) => this.handleChunk(res.data));
        } else {
            this.onStreamError({ errMsg: '当前基础库不支持流式接收，请升级微信或在真机调试' });
        }
        this.currentTask = task;
    },

    appendStreamingMessage() {
        const messages = this.data.messages.concat([{ key: 'm' + (this.msgSeq++), role: 'assistant', content: '', html: '' }]);
        this.streamingIndex = messages.length - 1;
        this.setData({ messages }, () => this.scrollToBottom());
    },

    handleChunk(arrayBuffer) {
        const bytes = new Uint8Array(arrayBuffer);
        for (let i = 0; i < bytes.length; i++) {
            this.byteBuffer.push(bytes[i]);
        }
        let newlineIndex;
        while ((newlineIndex = this.byteBuffer.indexOf(10)) !== -1) {
            const lineBytes = this.byteBuffer.slice(0, newlineIndex);
            this.byteBuffer = this.byteBuffer.slice(newlineIndex + 1);
            if (lineBytes.length && lineBytes[lineBytes.length - 1] === 13) {
                lineBytes.pop();
            }
            const line = utf8Decode(lineBytes);
            if (line) {
                this.handleSseLine(line);
            }
        }
    },

    handleSseLine(line) {
        if (line.indexOf('data:') !== 0) return;
        const payload = line.slice(5).trim();
        if (!payload || payload === '[DONE]') return;

        let json;
        try {
            json = JSON.parse(payload);
        } catch (e) {
            return;
        }
        if (json.done) {
            this.finishStreaming();
            return;
        }
        if (json.c) {
            this.appendToStreaming(json.c);
        }
    },

    appendToStreaming(chunk) {
        if (this.streamingIndex == null) return;
        this.streamingContent += chunk;
        this.setData({
            ['messages[' + this.streamingIndex + '].content']: this.streamingContent,
            ['messages[' + this.streamingIndex + '].html']: markdownToHtml(this.streamingContent)
        }, () => this.scrollToBottom());
    },

    finishStreaming() {
        if (!this.streaming) return;
        this.streaming = false;
        if (!this.streamingContent) {
            this.appendToStreaming('（未收到回复，请稍后再试）');
        }
        this.setData({ loading: false });
        this.currentTask = null;
    },

    onStreamError(err) {
        const reason = (err && (err.errMsg || err.msg)) ? (err.errMsg || err.msg) : '网络异常';
        if (this.streamingIndex != null && !this.streamingContent) {
            this.appendToStreaming('抱歉，连接失败了：' + reason);
        } else {
            this.pushMessage('assistant', '抱歉，连接失败了：' + reason);
        }
        this.streaming = false;
        this.setData({ loading: false });
        this.currentTask = null;
    },

    pushMessage(role, content) {
        const message = Object.assign({ key: 'm' + (this.msgSeq++) }, buildMessage(role, content));
        const messages = this.data.messages.concat([message]);
        this.setData({ messages }, () => this.scrollToBottom());
    },

    scrollToBottom() {
        const lastIndex = this.data.messages.length - 1;
        if (lastIndex >= 0) {
            this.setData({ scrollToId: 'msg-' + lastIndex });
        }
    }
});
