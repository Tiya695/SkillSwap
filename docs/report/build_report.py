"""
Builds the IP project report as a PDF.

Run from the project root (needs reportlab and Pillow):
    python docs/report/build_report.py

Pictures used (all in docs/report/assets): header.png, watermark.png and the six screenshots.
The report text has no hyphens or dashes anywhere (checked at the end).
"""
import os
import sys
from xml.sax.saxutils import escape

# packages installed for the current user
sys.path.insert(0, os.path.join(os.environ.get("APPDATA", ""), "Python", "Python314", "site-packages"))

from PIL import Image
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (BaseDocTemplate, CondPageBreak, Frame, HRFlowable, KeepTogether, PageBreak,
                                PageTemplate, Paragraph, Preformatted, Spacer, Table, TableStyle)
from reportlab.platypus import Image as RLImage

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
ASSETS = os.path.join(HERE, "assets")
OUT = os.path.join(ROOT, "docs", "SkillSwap_IP_Report.pdf")
SRC = os.path.join(ROOT, "backend", "src", "main", "java", "com", "skillswap")

# ------------------------------------------------------------------ fonts
FONTS = r"C:\Windows\Fonts"
pdfmetrics.registerFont(TTFont("Arial", os.path.join(FONTS, "arial.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Bold", os.path.join(FONTS, "arialbd.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Italic", os.path.join(FONTS, "ariali.ttf")))
pdfmetrics.registerFont(TTFont("Times", os.path.join(FONTS, "times.ttf")))
pdfmetrics.registerFont(TTFont("Times-Bold", os.path.join(FONTS, "timesbd.ttf")))
pdfmetrics.registerFont(TTFont("Code", os.path.join(FONTS, "consola.ttf")))
pdfmetrics.registerFontFamily("Arial", normal="Arial", bold="Arial-Bold", italic="Arial-Italic", boldItalic="Arial-Bold")
pdfmetrics.registerFontFamily("Times", normal="Times", bold="Times-Bold", italic="Times", boldItalic="Times-Bold")

PAGE_W, PAGE_H = A4
LEFT = RIGHT = 2.0 * cm
FRAME_TOP = 4.35 * cm      # space for the header picture
FRAME_BOTTOM = 1.9 * cm
CONTENT_W = PAGE_W - LEFT - RIGHT

NAVY = colors.HexColor("#1F2A44")
RULE = colors.HexColor("#8FAADC")

# ------------------------------------------------------------------ styles
body = ParagraphStyle("body", fontName="Arial", fontSize=10, leading=14.3, alignment=TA_JUSTIFY, spaceAfter=5)
h1 = ParagraphStyle("h1", fontName="Arial-Bold", fontSize=14.5, leading=18, spaceBefore=7, spaceAfter=2, textColor=colors.black)
h2 = ParagraphStyle("h2", fontName="Arial-Bold", fontSize=11.5, leading=15, spaceBefore=5, spaceAfter=3)
h3 = ParagraphStyle("h3", fontName="Arial-Bold", fontSize=10.5, leading=14, spaceBefore=4, spaceAfter=2)
bullet = ParagraphStyle("bullet", parent=body, leftIndent=0.9 * cm, bulletIndent=0.35 * cm, spaceAfter=2)
cell = ParagraphStyle("cell", fontName="Arial", fontSize=9, leading=11.5, alignment=TA_LEFT)
cell_b = ParagraphStyle("cell_b", parent=cell, fontName="Arial-Bold")
caption = ParagraphStyle("caption", fontName="Arial-Bold", fontSize=9.5, leading=12, alignment=TA_CENTER)
label = ParagraphStyle("label", fontName="Arial-Bold", fontSize=11, leading=15, spaceBefore=6, spaceAfter=4)
file_name = ParagraphStyle("file_name", fontName="Arial-Bold", fontSize=10.5, leading=14, spaceBefore=8, spaceAfter=3)
code_style = ParagraphStyle("code", fontName="Code", fontSize=8, leading=10, textColor=colors.black, leftIndent=8)
cover_big = ParagraphStyle("cover_big", fontName="Arial-Bold", fontSize=27, leading=34, alignment=TA_CENTER)
cover_name = ParagraphStyle("cover_name", fontName="Arial-Bold", fontSize=18, leading=24, alignment=TA_CENTER)
cover_sub = ParagraphStyle("cover_sub", fontName="Arial", fontSize=12, leading=17, alignment=TA_CENTER)
cover_bold = ParagraphStyle("cover_bold", fontName="Arial-Bold", fontSize=13, leading=19, alignment=TA_CENTER)


def P(text, style=body):
    return Paragraph(text, style)


def esc(text):
    return escape(text)


def heading1(text):
    return [CondPageBreak(3.1 * cm), Paragraph(esc(text), h1),
            HRFlowable(width="100%", thickness=0.9, color=RULE, spaceBefore=1, spaceAfter=5)]


def heading2(text):
    return [CondPageBreak(2.6 * cm), Paragraph(esc(text), h2)]


def heading3(text):
    return [CondPageBreak(2.1 * cm), Paragraph(esc(text), h3)]


def bullets(items):
    out = []
    for item in items:
        out.append(Paragraph(item, bullet, bulletText="\u2022"))
    return out


def grid_table(rows, widths, header=True, bold_first=True):
    data = []
    for r, row in enumerate(rows):
        data.append([Paragraph(c, cell_b if (header and r == 0) or (bold_first and i == 0) else cell)
                     for i, c in enumerate(row)])
    t = Table(data, colWidths=widths, repeatRows=1 if header else 0)
    style = [("GRID", (0, 0), (-1, -1), 0.6, colors.HexColor("#9A9A9A")),
             ("VALIGN", (0, 0), (-1, -1), "TOP"),
             ("LEFTPADDING", (0, 0), (-1, -1), 6), ("RIGHTPADDING", (0, 0), (-1, -1), 6),
             ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3)]
    if header:
        style.append(("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#F0F0F0")))
    t.setStyle(TableStyle(style))
    return t


# ------------------------------------------------------------------ pictures
def prepare_assets():
    """Makes sure header.png, watermark.png and the screenshots exist as PNG files."""
    header = os.path.join(ASSETS, "header.png")
    mark = os.path.join(ASSETS, "watermark_clear.png")
    # watermark: the supplied picture has a grey background, make that part see through
    src = Image.open(os.path.join(ASSETS, "watermark.png")).convert("RGB")
    bg = src.getpixel((2, 2))
    out = Image.new("RGBA", src.size)
    px, dst = src.load(), out.load()
    for y in range(src.size[1]):
        for x in range(src.size[0]):
            r, g, b = px[x, y]
            d = max(abs(r - bg[0]), abs(g - bg[1]), abs(b - bg[2]))
            dst[x, y] = (r, g, b, min(165, d * 14))
    out.save(mark)
    return header, mark


HEADER, WATERMARK = prepare_assets()
_hdr = Image.open(HEADER)
HEADER_W = PAGE_W - 2 * 1.45 * cm
HEADER_H = HEADER_W * _hdr.size[1] / _hdr.size[0]
_wm = Image.open(WATERMARK)
WM_W = 13.2 * cm
WM_H = WM_W * _wm.size[1] / _wm.size[0]


def decorate(canvas, doc):
    """Page border, watermark and header picture on every page."""
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#555555"))
    canvas.setLineWidth(0.8)
    canvas.rect(1.0 * cm, 1.0 * cm, PAGE_W - 2.0 * cm, PAGE_H - 2.0 * cm)
    canvas.drawImage(WATERMARK, (PAGE_W - WM_W) / 2, (PAGE_H - 3.6 * cm - WM_H) / 2 + 0.2 * cm, WM_W, WM_H, mask="auto")
    canvas.drawImage(HEADER, (PAGE_W - HEADER_W) / 2, PAGE_H - 1.3 * cm - HEADER_H, HEADER_W, HEADER_H, mask="auto")
    canvas.restoreState()


# ------------------------------------------------------------------ source code excerpts
def read_src(relative):
    with open(os.path.join(SRC, relative), encoding="utf-8") as f:
        return f.read().replace("\t", "    ")


def cut(text, start, end=None):
    a = text.index(start)
    b = text.index(end, a) if end else len(text)
    return text[a:b].rstrip()


def strip_imports(text):
    lines = [ln for ln in text.splitlines() if not ln.startswith("import ")]
    out = "\n".join(lines)
    while "\n\n\n" in out:
        out = out.replace("\n\n\n", "\n\n")
    return out.strip()


def code_block(text):
    """A few lines of code on the plain white page."""
    lines = text.splitlines()
    for ln in lines:
        assert len(ln) <= 105, f"code line too long ({len(ln)}): {ln}"
    return [Preformatted(text, code_style)]


# ------------------------------------------------------------------ the story
def cover():
    s = [Spacer(1, 2.3 * cm), P("IP PROJECT REPORT", cover_big), Spacer(1, 1.7 * cm),
         P("SkillSwap", cover_name), Spacer(1, 0.2 * cm), P("A Skill Exchange Platform", cover_sub), Spacer(1, 1.6 * cm),
         P("PRESENTED BY: TT-AI&amp;ML-C", cover_bold), P("Semester V", cover_bold),
         Spacer(1, 0.6 * cm), P("Tiya Rai [03]", cover_sub), Spacer(1, 0.5 * cm),
         P("Academic Year 2026-27", cover_sub), PageBreak()]
    return s


def build_story():
    s = cover()

    # 1 -------------------------------------------------------------------
    s += heading1("1. Abstract")
    s.append(P(
        "SkillSwap is a website where people exchange skills instead of paying for lessons. A person lists the skills they "
        "can teach and the skills they want to learn, and the system finds people whose lists fit together. Two people who "
        "can each teach the other something become a mutual match and can start a swap. The server is built with Spring "
        "Boot, the pages with React, and all the data is saved in a MySQL database."))
    s.append(P(
        "When a swap is accepted, the two people get a chat, a video call link and a learning period of the days they agreed "
        "on. At the end each person writes a multiple choice test for the other. The application marks the test by itself, "
        "and when both people score 50 percent or more, the swap is completed and they can rate each other."))

    # 2 -------------------------------------------------------------------
    s += heading1("2. Introduction")
    s.append(P(
        "Many people know something well and want to learn something else, but paying for classes is not always possible. "
        "SkillSwap lets them trade what they know. Someone who knows Python can teach it to a person who plays the guitar, "
        "and the guitar player can teach back. No money is involved. To register, a person needs a Gmail address, a date of "
        "birth and a strong password, and a forgotten password can be reset with the date of birth."))
    s += heading3("Objectives")
    s += bullets([
        "To let people list the skills they can teach and the skills they want to learn.",
        "To find matches automatically and show the mutual matches first.",
        "To let two people send, accept, reject or cancel a swap and agree on the number of days.",
        "To give every accepted swap a chat and a video call link.",
        "To check what was learned with a multiple choice test that the application marks.",
        "To save all data in a real MySQL database and keep passwords safe.",
    ])

    # 3 -------------------------------------------------------------------
    s += heading1("3. Project Description")
    s += heading2("3.1 System Architecture")
    s.append(P(
        "SkillSwap has two programs that talk to each other. The React website runs in the browser and sends requests to the "
        "Spring Boot server. The server is split into simple layers, and every error is sent back in the same format so the "
        "website can show it under the right field."))
    s.append(grid_table([
        ["Layer", "Files and Responsibility"],
        ["Controller", "REST controllers that receive a request and send back the answer, one for each area"],
        ["Service", "The rules: matching, swap rules, tests and marking, password reset and account deletion"],
        ["Repository", "Spring Data JPA interfaces that read and write the MySQL tables"],
        ["Entity", "Java classes for the tables: User, Skill, UserSkill, SwapRequest, SwapMessage, Assessment and Review"],
        ["Security", "JwtUtil, JwtAuthFilter and SecurityConfig: login tokens, password hashing and admin only actions"],
        ["Website", "React pages, reusable parts such as cards, forms, chat and test screens, and the API files"],
    ], [3.0 * cm, CONTENT_W - 3.0 * cm]))
    s.append(Spacer(1, 4))

    s += heading2("3.2 Key Components Used")
    s += bullets([
        "<b>Spring Boot, Spring Data JPA and Hibernate:</b> run the server and create the MySQL tables on the first start.",
        "<b>Spring Security, JWT and BCrypt:</b> check the login token on every request and save passwords as hashes.",
        "<b>MySQL and Connector/J:</b> the real database and its driver.",
        "<b>React, Vite and Tailwind CSS:</b> build the pages in the light purple and white design.",
        "<b>Swagger UI, JUnit and Mockito:</b> try every server call and run 92 automatic tests.",
        "<b>Ollama and Jitsi Meet:</b> a small AI model on the same computer drafts test questions, and Jitsi gives a free call room.",
    ])

    s += heading2("3.3 Module Descriptions")
    s.append(P(
        "<b>Registration and Login.</b> A person registers with a name, a Gmail address, a date of birth and a password with "
        "six or more characters, an uppercase letter, a lowercase letter, a digit and a special character. A forgotten "
        "password is reset with the Gmail address and the date of birth. The login is kept per browser tab, so two people can "
        "be logged in on two tabs."))
    s.append(P(
        "<b>Profile.</b> A person can change the name, bio, date of birth and a ten digit Indian mobile number, choose a "
        "profile photo from the computer, and delete the account after entering the password again."))
    s.append(P(
        "<b>Skills and Matching.</b> People add skills from a shared list to what they can teach or want to learn, and can "
        "create a new skill if it is missing. If another person teaches what I want to learn and wants what I teach, it is "
        "a mutual match. If only one side fits, it is a one way match."))
    s.append(P(
        "<b>Swaps and Workspace.</b> From a match a person sends a request with the skills, the number of days and a "
        "message. After it is accepted, the two people get a learning period, a chat that refreshes every three seconds and a "
        "Jitsi call link that either person can replace with their own link."))
    s.append(P(
        "<b>Final Tests.</b> When the learning period ends, each person writes a multiple choice test for the other, or asks "
        "the local AI model for a draft from a topic and then edits it. The application marks the answers at once, gives "
        "points and says whether the person passed. The pass mark is 50 percent, and a failed test can be taken again."))
    s.append(P(
        "<b>Reviews and Admin.</b> After a completed swap each person gives one to five stars. The admin can add, change and "
        "delete skills in the shared list."))

    s += heading2("3.4 Database Schema")
    s.append(P("The server creates the MySQL database skillswap and its eight tables on the first start:"))
    s.append(grid_table([
        ["Table", "Primary Key", "Key Fields"],
        ["users", "id", "name, email, password (hash), date_of_birth, phone, avatar_url, role"],
        ["skills", "id", "name, category, description"],
        ["user_skills", "id", "user_id, skill_id, type (teach or learn), level"],
        ["swap_requests", "id", "requester_id, receiver_id, skills, status, duration_days, start_date, end_date"],
        ["swap_messages", "id", "swap_request_id, sender_id, text, created_at"],
        ["assessments", "id", "swap_request_id, teacher_id, learner_id, status, score_percent, passed"],
        ["assessment_questions", "id", "assessment_id, text, options_json, correct_option, selected_option"],
        ["reviews", "id", "swap_request_id, reviewer_id, reviewee_id, rating, comment"],
    ], [4.2 * cm, 2.2 * cm, CONTENT_W - 6.4 * cm]))
    s.append(Spacer(1, 4))

    s += heading2("3.5 Software and Hardware Requirements")
    req = Table([[
        [Paragraph("<b>Software</b>", cell)] + [Paragraph(t, cell) for t in [
            "Java (JDK 21 or newer), Spring Boot 3.5",
            "React with Vite (Node.js 20 or newer)",
            "MySQL 8.0 or newer, Connector/J 9.6.0",
            "VS Code or IntelliJ IDEA, Postman",
            "Optional: Ollama with the phi3 model"]],
        [Paragraph("<b>Hardware</b>", cell)] + [Paragraph(t, cell) for t in [
            "Processor: Intel i3 or above",
            "RAM: Minimum 8 GB (16 GB with Ollama)",
            "Storage: 2 GB free space",
            "Display: 1280x768 minimum resolution",
            "Internet: needed only for the video call"]],
    ]], colWidths=[CONTENT_W / 2, CONTENT_W / 2])
    req.setStyle(TableStyle([("GRID", (0, 0), (-1, -1), 0.6, colors.HexColor("#9A9A9A")), ("VALIGN", (0, 0), (-1, -1), "TOP"),
                             ("LEFTPADDING", (0, 0), (-1, -1), 8), ("TOPPADDING", (0, 0), (-1, -1), 4),
                             ("BOTTOMPADDING", (0, 0), (-1, -1), 4)]))
    s.append(req)

    # 4 -------------------------------------------------------------------
    s += heading1("4. Results and Discussion")
    s.append(P(
        "SkillSwap was built and tested against the planned features. A person can register, log in, add skills, find "
        "matches, send a swap, chat, take a test and finish a swap from start to end. The project has 92 automatic tests, "
        "including one that follows a whole swap from the request to completion with a failed test and a retake. All of "
        "them pass."))
    s.append(P(
        "Passwords are saved as hashes, and every request except login, register and password reset needs a valid token. "
        "Only the admin can edit or delete shared skills, and only the two people of a swap can read its chat and tests. "
        "Every form shows problems under the field, and each save shows a green success message."))
    s.append(P(
        "The AI question writer needs about one minute for five questions on a computer without a graphics card. A small "
        "model can make mistakes, so the teacher checks the questions and the right answer before sending."))
    s.append(P(
        "The pictures below are taken from the running project. Figure 1 is the landing page. Figure 2 is the dashboard with "
        "the active swaps, the mutual matches and the latest swap activity. Figure 3 is the list of skills that can be "
        "searched and added, and Figure 4 shows a person's own skills to teach and to learn. Figure 5 shows the matches with "
        "the fitting skills of both sides, and Figure 6 shows the swaps with their status and the buttons to act on them."))
    s.extend(figure_block([
        ("landing.png", "Figure 1: Landing Page"), ("dashboard.png", "Figure 2: Dashboard"),
        ("browse.png", "Figure 3: Browse Skills"), ("myskills.png", "Figure 4: My Skills"),
        ("matches.png", "Figure 5: Matches"), ("swaps.png", "Figure 6: Swaps"),
    ]))

    # 5 -------------------------------------------------------------------
    s += heading1("5. Conclusion and Future Scope")
    s += heading3("Conclusion")
    s.append(P(
        "SkillSwap shows that people can learn from each other without paying. It joins a Spring Boot server, a React website "
        "and a MySQL database into one working system, from registration to a completed and reviewed swap. The project gave "
        "practice in building web services, saving data with JPA, protecting a website with tokens and writing automatic tests."))
    s += heading3("Future Scope")
    s += bullets([
        "Send a password reset link by email instead of asking for the date of birth.",
        "Add a video call inside the website and send notifications for new requests and messages.",
        "Give a certificate when a swap is completed and show the admin simple charts.",
        "Make a mobile app for Android and iOS.",
        "Add other question types, such as short written answers, and explain on each match why two people fit.",
    ])

    # 6 -------------------------------------------------------------------
    s += heading1("6. References")
    s += bullets([
        "Spring Boot and Spring Security Reference Documentation, spring.io",
        "MySQL 8.0 Reference Manual and Connector/J Documentation, dev.mysql.com",
        "React Documentation, react.dev, and Tailwind CSS Documentation, tailwindcss.com",
        "JSON Web Token Introduction, jwt.io",
        "Ollama Documentation, ollama.com, and Jitsi Meet, jitsi.org",
        "Hibernate ORM Documentation, hibernate.org",
        "Baeldung Spring Tutorials, baeldung.com",
        "GeeksforGeeks Spring Boot Guide, geeksforgeeks.org",
    ])

    # 7 -------------------------------------------------------------------
    s.append(PageBreak())
    s += heading1("7. Annexure (Source Code)")
    s.append(P(
        "A few short parts of the source code are shown below. The complete project is in the backend and frontend folders."))

    def add(title, text):
        s.append(CondPageBreak(2.2 * cm))
        s.append(Paragraph(esc(title), file_name))
        s.extend(code_block(text))

    add("User.java (the users table)", strip_imports(cut(read_src("entity/User.java"), "// JPA entity")))
    add("SwapRequestController.java (swap requests)",
        strip_imports(cut(read_src("controller/SwapRequestController.java"), "@RestController")))
    marking = cut(read_src("service/AssessmentService.java"), "        int total = 0;", "        assessment.setStatus(AssessmentStatus.GRADED);")
    add("AssessmentService.java (marking a test)", marking)
    unlocked = cut(read_src("entity/SwapRequest.java"), "    /** Assessments open when", "    @PrePersist")
    add("SwapRequest.java (when the tests open)", unlocked)

    return s


def figure_block(items):
    """OUTPUT label and the pictures, two in a row with the figure name under each picture. Kept on one page."""
    gap = 0.5 * cm
    col = (CONTENT_W - gap) / 2
    pic_w = col - 0.1 * cm
    rows = []
    for i in range(0, len(items), 2):
        pair = items[i:i + 2]
        imgs, caps = [], []
        for name, text in pair:
            path = os.path.join(ASSETS, name)
            w, h = Image.open(path).size
            imgs.append(RLImage(path, width=pic_w, height=pic_w * h / w))
            caps.append(Paragraph(esc(text), caption))
        # the OUTPUT label stays with the first row; each row (two pictures and their names) stays whole
        group = ([Paragraph("OUTPUT:", label)] if i == 0 else []) + two_col_row(imgs, caps, col, gap)
        rows.append(KeepTogether(group))
        rows.append(Spacer(1, 0.2 * cm))
    return rows


def two_col_row(imgs, caps, col, gap):
    t = Table([[imgs[0], "", imgs[1]], [caps[0], "", caps[1]]], colWidths=[col, gap, col])
    t.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("ALIGN", (0, 0), (-1, -1), "CENTER"),
                           ("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 0),
                           ("TOPPADDING", (0, 0), (-1, 0), 0), ("BOTTOMPADDING", (0, 0), (-1, 0), 3),
                           ("TOPPADDING", (0, 1), (-1, 1), 2), ("BOTTOMPADDING", (0, 1), (-1, 1), 4),
                           ("BOX", (0, 0), (0, 0), 0.5, colors.HexColor("#BBBBBB")),
                           ("BOX", (2, 0), (2, 0), 0.5, colors.HexColor("#BBBBBB"))]))
    return [t]


# ------------------------------------------------------------------ build
def main():
    doc = BaseDocTemplate(OUT, pagesize=A4, leftMargin=LEFT, rightMargin=RIGHT, topMargin=FRAME_TOP, bottomMargin=FRAME_BOTTOM,
                          title="SkillSwap IP Project Report", author="Tiya Rai")
    frame = Frame(LEFT, FRAME_BOTTOM, CONTENT_W, PAGE_H - FRAME_TOP - FRAME_BOTTOM, id="main",
                  leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0)
    doc.addPageTemplates([PageTemplate(id="page", frames=[frame], onPage=decorate)])
    doc.build(build_story())
    print("written", OUT)


if __name__ == "__main__":
    main()
