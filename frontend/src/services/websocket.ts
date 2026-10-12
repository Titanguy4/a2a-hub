import { Client, type IMessage } from '@stomp/stompjs';
import type { AgentStatusEvent } from './api';

type StatusEventHandler = (event: AgentStatusEvent) => void;

class WebSocketService {
  private client: Client | null = null;
  private listeners: StatusEventHandler[] = [];
  public isConnected = false;

  public connect(brokerUrl?: string) {
    if (this.client && this.client.active) {
      return;
    }

    const targetUrl = this.resolveBrokerUrl(brokerUrl);

    this.client = new Client({
      brokerURL: targetUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        this.isConnected = true;
        console.log('[WebSocket] Connected to A2A Hub broker at:', targetUrl);

        this.client?.subscribe('/topic/agents/status', (message: IMessage) => {
          try {
            const event: AgentStatusEvent = JSON.parse(message.body);
            this.listeners.forEach(fn => fn(event));
          } catch (e) {
            console.error('[WebSocket] Failed to parse agent status event:', e);
          }
        });
      },
      onDisconnect: () => {
        this.isConnected = false;
        console.log('[WebSocket] Disconnected from broker');
      },
      onStompError: frame => {
        console.error('[WebSocket] STOMP Broker error:', frame.headers['message'], frame.body);
      }
    });

    this.client.activate();
  }

  public onStatusChange(callback: StatusEventHandler) {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter(l => l !== callback);
    };
  }

  public disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  private resolveBrokerUrl(brokerUrl?: string): string {
    const configuredUrl: string | undefined = import.meta.env.VITE_WS_URL;
    return brokerUrl || (configuredUrl && this.toAbsoluteWsUrl(configuredUrl)) || this.defaultBrokerUrl();
  }

  private toAbsoluteWsUrl(url: string): string {
    return url.startsWith('/') ? this.wsScheme() + window.location.host + url : url;
  }

  private defaultBrokerUrl(): string {
    return this.wsScheme() + (import.meta.env.VITE_WS_HOST || 'localhost:8080') + '/ws';
  }

  private wsScheme(): string {
    return window.location.protocol === 'https:' ? 'wss://' : 'ws://';
  }
}

export const wsService = new WebSocketService();
