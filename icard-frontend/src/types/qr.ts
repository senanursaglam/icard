export interface QRCodeResponse {
  code: string;
  expireDate: string;
}

export interface QRVerifyRequest {
  code: string;
}