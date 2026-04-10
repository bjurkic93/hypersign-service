<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            font-size: 15px;
            color: #111827;
            background-color: #f8fafc;
            margin: 0;
            padding: 0;
            line-height: 1.6;
        }
        .wrapper {
            background: linear-gradient(180deg, rgba(220, 38, 38, 0.08) 0%, #f8fafc 40%, #f1f5f9 100%);
            padding: 48px 24px;
        }
        .container {
            max-width: 480px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 24px;
            border: 1px solid rgba(31, 41, 55, 0.1);
            box-shadow: 0 24px 70px rgba(15, 23, 42, 0.12);
            padding: 40px 36px;
        }
        .logo {
            margin-bottom: 24px;
        }
        .logo img {
            height: 44px;
            width: auto;
        }
        .title {
            font-size: 26px;
            font-weight: 700;
            color: #111827;
            margin: 0 0 8px 0;
            letter-spacing: -0.02em;
        }
        .subtitle {
            font-size: 15px;
            color: #9ca3af;
            margin: 0 0 28px 0;
        }
        .text {
            color: #374151;
            margin: 0 0 16px 0;
        }
        .greeting {
            font-weight: 600;
            color: #111827;
        }
        .voucher-container {
            background: linear-gradient(135deg, rgba(34, 197, 94, 0.08), rgba(34, 197, 94, 0.04));
            border: 1px solid rgba(34, 197, 94, 0.2);
            border-radius: 16px;
            padding: 24px;
            margin: 28px 0;
            text-align: center;
        }
        .voucher-label {
            font-size: 12px;
            font-weight: 600;
            color: #9ca3af;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin: 0 0 8px 0;
        }
        .voucher-code {
            font-size: 28px;
            font-weight: 700;
            letter-spacing: 4px;
            color: #16a34a;
            margin: 0;
            font-family: 'SF Mono', Monaco, 'Courier New', monospace;
        }
        .discount-badge {
            display: inline-block;
            background: linear-gradient(135deg, #16a34a, #15803d);
            color: #ffffff;
            border-radius: 12px;
            padding: 12px 24px;
            font-size: 24px;
            font-weight: 700;
            margin: 16px 0 0 0;
        }
        .expiry {
            display: inline-block;
            background: #f1f5f9;
            border-radius: 8px;
            padding: 8px 14px;
            font-size: 13px;
            color: #6b7280;
            margin: 20px 0;
        }
        .expiry strong {
            color: #111827;
        }
        .info-box {
            background: #f8fafc;
            border: 1px solid rgba(31, 41, 55, 0.1);
            border-radius: 12px;
            padding: 16px;
            margin: 24px 0;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid rgba(31, 41, 55, 0.06);
        }
        .info-row:last-child {
            border-bottom: none;
        }
        .info-label {
            color: #6b7280;
            font-size: 14px;
        }
        .info-value {
            color: #111827;
            font-weight: 600;
            font-size: 14px;
        }
        .footer {
            margin-top: 36px;
            padding-top: 24px;
            border-top: 1px solid rgba(31, 41, 55, 0.08);
        }
        .footer-text {
            font-size: 13px;
            color: #9ca3af;
            margin: 0 0 16px 0;
        }
        .signature {
            font-size: 13px;
            color: #6b7280;
            margin: 0;
        }
        .signature strong {
            color: #111827;
            display: block;
            margin-top: 4px;
        }
    </style>
</head>

<body>
<div class="wrapper">
    <div class="container">

        <div class="logo">
            <img src="https://static.wixstatic.com/media/39eefc_626500717c344fe8969174cc515e68d4~mv2.png/v1/fill/w_211,h_50,al_c,q_85,usm_0.66_1.00_0.01,enc_avif,quality_auto/rdx-logo-final.png" alt="RdX">
        </div>

        <h1 class="title">Čestitamo!</h1>
        <p class="subtitle">Zaradili ste popust voucher kao nagradu.</p>

        <p class="text"><span class="greeting">Pozdrav${userName???then(", " + userName, "")}!</span></p>

        <p class="text">
            Hvala vam što ste prikazivali reklamu na vašem uređaju. 
            Kao nagradu za vašu lojalnost, zaradili ste sljedeći popust:
        </p>

        <div class="voucher-container">
            <p class="voucher-label">Vaš voucher kod</p>
            <p class="voucher-code">${voucherCode}</p>
            <div class="discount-badge">${discountPercentage}% POPUST</div>
        </div>

        <div style="text-align: center;">
            <span class="expiry">Vrijedi do <strong>${expiresAt}</strong></span>
        </div>

        <#if description?has_content>
        <div class="info-box">
            <div class="info-row">
                <span class="info-label">Opis:</span>
                <span class="info-value">${description}</span>
            </div>
        </div>
        </#if>

        <div class="info-box">
            <div class="info-row">
                <span class="info-label">Reklama:</span>
                <span class="info-value">${advertisementName}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Organizacija:</span>
                <span class="info-value">${organizationName}</span>
            </div>
        </div>

        <div class="footer">
            <p class="footer-text">
                Iskoristite ovaj kod prilikom sljedeće kupovine kod partnera ${organizationName}.
            </p>

            <p class="signature">
                Srdačan pozdrav,
                <strong>RdX Tim</strong>
            </p>
        </div>

    </div>
</div>
</body>
</html>
