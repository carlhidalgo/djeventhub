#!/usr/bin/env python3
# Script de respaldo: convierte un archivo Markdown a PDF usando markdown + xhtml2pdf
# Uso: python convert_md_to_pdf.py <input_md_path> <output_pdf_path>

import sys
import io
import os

try:
    import markdown
    from xhtml2pdf import pisa
except Exception as e:
    print("Módulos necesarios no instalados:", e)
    print("Ejecuta: pip install markdown xhtml2pdf")
    sys.exit(2)


def convert_md_to_pdf(input_md, output_pdf):
    with open(input_md, 'r', encoding='utf-8') as f:
        md_text = f.read()
    html_body = markdown.markdown(md_text, extensions=['extra', 'tables'])
    full_html = f"""
    <html>
    <head>
      <meta charset='utf-8'>
      <style>
        body { font-family: DejaVu Sans, Arial, Helvetica, sans-serif; font-size: 12pt; }
        h1 { font-size: 20pt; }
        h2 { font-size: 16pt; }
        pre, code { font-family: monospace; background: #f4f4f4; padding: 4px; }
        table { border-collapse: collapse; }
        table, th, td { border: 1px solid #ccc; padding: 4px; }
      </style>
    </head>
    <body>
    {html_body}
    </body>
    </html>
    """

    # xhtml2pdf expects a byte stream for destination
    result = pisa.CreatePDF(io.StringIO(full_html), dest=open(output_pdf, 'wb'))
    return result.err


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Uso: python convert_md_to_pdf.py input.md output.pdf')
        sys.exit(1)
    inp = sys.argv[1]
    out = sys.argv[2]
    if not os.path.exists(inp):
        print('Archivo de entrada no encontrado:', inp)
        sys.exit(1)
    code = convert_md_to_pdf(inp, out)
    if code == 0:
        print('PDF generado:', out)
        sys.exit(0)
    else:
        print('Error generando PDF, codigo:', code)
        sys.exit(3)

